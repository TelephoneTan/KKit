package promise

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import synchronizedAsync
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

class JobResult private constructor() {
    companion object {
        internal val INSTANCE = JobResult()
    }
}

class PromiseState<RESULT>(
    internal val self: Promise<RESULT>
)

interface ResolvePack<RESULT> : PromiseScope, PromiseCancelledBroadcast {
    override val scopeCancelledBroadcast: PromiseCancelledBroadcast
    override val isActive: Boolean
        get() = scopeCancelledBroadcast.isActive

    override fun listen(r: () -> Unit) = scopeCancelledBroadcast.listen(r)

    override fun unListen(key: PromiseCancelledBroadcastKey) = scopeCancelledBroadcast.unListen(key)

    suspend fun rsv(v: RESULT): JobResult
    suspend fun rsp(p: Promise<RESULT>): JobResult
    suspend fun rej(e: Throwable): JobResult
    suspend fun state(): PromiseState<RESULT>
}

interface ValuePack<VALUE, RESULT> : ResolvePack<RESULT> {
    val value: VALUE
}

interface ReasonPack<RESULT> : ResolvePack<RESULT> {
    val reason: Throwable
}

interface ForwardPack<RESULT> : ResolvePack<RESULT> {
    suspend fun forward(): JobResult
}

interface JobPack<RESULT> : ResolvePack<RESULT> {
    fun waiting(): JobResult = JobResult.INSTANCE
}

enum class Status {
    RUNNING,
    SUCCEED,
    FAILED,
    CANCELLED,
}

typealias PromiseJob<RESULT> = suspend JobPack<RESULT>.() -> JobResult
typealias SucceededHandler<RESULT, NEXT_RESULT> = suspend ValuePack<RESULT, NEXT_RESULT>.() -> JobResult
typealias SucceededConsumer<RESULT> = suspend ValuePack<RESULT, Any?>.() -> Unit
typealias FailedHandler<NEXT_RESULT> = suspend ReasonPack<NEXT_RESULT>.() -> JobResult
typealias FailedConsumer = suspend ReasonPack<Any?>.() -> Unit
typealias CancelledListener = suspend () -> Unit
typealias FinallyHandler<LAST_RESULT> = suspend ForwardPack<LAST_RESULT>.() -> JobResult
typealias FinallyConsumer<LAST_RESULT> = suspend ForwardPack<LAST_RESULT>.() -> Unit

data class PromiseConfig internal constructor(
    val semaphore: PromiseSemaphore?,
    val scopeCancelledBroadcast: PromiseCancelledBroadcast?,
    internal val shouldWrapJobWithSemaphore: Boolean
) {
    constructor(
        scopeCancelledBroadcast: PromiseCancelledBroadcast? = null,
        semaphore: PromiseSemaphore? = null,
    ) : this(semaphore, scopeCancelledBroadcast, true)

    companion object {
        val EMPTY_CONFIG = PromiseConfig()
    }
}

class Promise<RESULT> private constructor(
    @Suppress("MemberVisibilityCanBePrivate")
    val job: PromiseJob<RESULT>?,
    private val config: PromiseConfig,
) {
    constructor(
        config: PromiseConfig?,
        job: PromiseJob<RESULT>,
    ) : this(job, config ?: PromiseConfig.EMPTY_CONFIG)

    @Volatile
    private var status = Status.RUNNING
    val state get() = status
    private val settled = Channel<Any>()
    private val submit = Channel<Any?>(1)
    private val setTimeoutMutex = Mutex()
    private var timeoutSN = -1
    private var timeoutTriggered = false
    private var value: RESULT? = null
    private var reason: Throwable? = null
    private val semaphoreMutex = Mutex()
    private var semaphoreAcquired = false
    private var semaphoreReleased = false

    private suspend fun acquireSemaphore() {
        config.semaphore?.run semaphoreRun@{
            this@semaphoreRun.acquire()
            semaphoreMutex.withLock {
                semaphoreAcquired = true
                if (semaphoreReleased) {
                    this@semaphoreRun.release()
                    throw kotlin.coroutines.cancellation.CancellationException()
                }
            }
        }
    }

    private fun releaseSemaphoreAsync() {
        config.semaphore?.run semaphoreRun@{
            semaphoreMutex.synchronizedAsync {
                if (semaphoreAcquired) {
                    this@semaphoreRun.release()
                }
                semaphoreReleased = true
            }
        }
    }

    private val runningJob: Job? = job?.run jobRun@{
        if (config.shouldWrapJobWithSemaphore) {
            wrap@{
                acquireSemaphore()
                this@jobRun.invoke(this@wrap)
            }
        } else {
            this@jobRun
        }
    }?.let {
        CoroutineScope(Dispatchers.Default + CoroutineExceptionHandler { _, _ ->
            // 不在这里捕获异常，因为 CancellationException 不会在这里被捕获，而实际上
            // 并不是所有内部抛出的 CancellationException 都意味着此 Job 已经被取消。
            // 例如在此 Job 内部使用 withTimeout(){} 时，该代码块超时后会抛出
            // TimeoutCancellationException（是 CancellationException 的一个子类），
            // 但这种情形下该 TimeoutCancellationException 是 Job 内部逻辑抛出的异常，
            // 与此 Job 的取消状态无关，该异常应被捕获并使此 promise.Promise 转为“失败”状态。
            // 因为这里无法捕获所有该被捕获的异常，所以将异常捕获放在 invokeOnCompletion 中。
        }).launch {
            perform(coroutineContext) {
                it(object : JobPack<RESULT>, ResolvePack<RESULT> by this {})
            }
        }.apply {
            invokeOnCompletion {
                when (it) {
                    is CancellationException -> if (status != Status.CANCELLED) fail(it)
                    is Throwable -> fail(it)
                }
            }
        }
    }
    private val cancelledBroadcaster: PromiseCancelledBroadcaster =
        PromiseCancelledBroadcaster.new()

    // 此字段必须放在最后一个，因为 cancel 方法可能会被立即调用
    private val scopeUnListenKey = config.scopeCancelledBroadcast?.listen(this::cancel)

    private fun settle(status: Status, op: () -> Unit = {}): Boolean {
        return submit.trySend(null).takeIf { it.isSuccess }?.run {
            op()
            this@Promise.status = status
            settled.close()
            // scopeUnListenKey 为空时不一定表示没有监听，有可能是初始化字段时已经取消导致 cancel 被立即调用
            scopeUnListenKey?.let { config.scopeCancelledBroadcast?.unListen(it) }
            releaseSemaphoreAsync()
            if (status == Status.CANCELLED) {
                runningJob?.cancel()
                cancelledBroadcaster.broadcast()
            }
        }?.let { true } ?: false
    }

    private fun succeed(v: RESULT) = settle(Status.SUCCEED) { value = v }

    private fun fail(e: Throwable) = settle(Status.FAILED) { reason = e }

    fun cancel() = settle(Status.CANCELLED)

    val onSettled get() = settled.onReceiveCatching

    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun awaitSettled() {
        select {
            onSettled { }
        }
    }

    private suspend fun perform(
        context: CoroutineContext,
        fixedPromise: Array<Promise<RESULT>?>? = null,
        op: suspend ForwardPack<RESULT>.() -> Unit,
    ) {
        val s = PromiseState(this)
        op(object : ForwardPack<RESULT> {
            private suspend fun <T> context(block: suspend () -> T) =
                CoroutineScope(context).async { block() }.await()

            override suspend fun rsv(v: RESULT) = context {
                fixedPromise?.get(0)?.run {
                    forward()
                } ?: succeed(v)
                JobResult.INSTANCE
            }

            override suspend fun rsp(p: Promise<RESULT>) = context {
                transfer(false, p, fixedPromise)
                JobResult.INSTANCE
            }

            override suspend fun rej(e: Throwable) = context {
                fail(e)
                JobResult.INSTANCE
            }

            override suspend fun forward() = context {
                rsp(fixedPromise!!.let { it[0]!!.apply { it[0] = null } })
            }

            override suspend fun state() = context {
                s
            }

            override val scopeCancelledBroadcast = PromiseCancelledBroadcast.merge(
                *arrayOf(
                    this@Promise.config.scopeCancelledBroadcast,
                    this@Promise.cancelledBroadcaster
                )
            )
        })
    }

    private suspend fun <ANOTHER> transfer(
        shouldAcquireSemaphore: Boolean,
        from: Promise<ANOTHER>,
        fixedPromise: Array<Promise<RESULT>?>? = null,
        onSucceeded: SucceededHandler<ANOTHER, RESULT> = {
            @Suppress("UNCHECKED_CAST")
            rsv(value as RESULT)
        },
        onFailed: FailedHandler<RESULT> = {
            rej(reason)
        },
        onCancelled: CancelledListener = {},
        onFinally: FinallyHandler<ANOTHER>? = null,
    ) {
        val context = coroutineContext
        val fixedPromiseF: Array<Promise<RESULT>?>? =
            if (onFinally != null)
                arrayOf(@Suppress("UNCHECKED_CAST") (from as Promise<RESULT>))
            else
                fixedPromise
        var selfCancelled = false
        try {
            from.awaitSettled()
        } catch (e: CancellationException) {
            selfCancelled = true
        }
        if (shouldAcquireSemaphore) {
            acquireSemaphore()
        }
        val runCancelCallback: suspend (Boolean) -> Unit = { isSelfCancelled ->
            perform(context, fixedPromiseF) perform@{
                val callback: suspend () -> Unit = {
                    if (onFinally != null) {
                        onFinally(
                            @Suppress("UNCHECKED_CAST")
                            (this@perform as ForwardPack<ANOTHER>)
                        )
                    } else {
                        onCancelled()
                    }
                }
                // if it is `from` instead of ourselves that is cancelled,
                // we should immediately cancel ourselves before running
                // callbacks, for 2 reasons:
                //
                // <1> the callbacks may change our state, but our state
                // should be "cancelled" if `from` is cancelled.
                //
                // <2> the callbacks may consume time, so our state will get
                // to "cancelled" slowly, this is bad.
                if (!isSelfCancelled) {
                    this@Promise.cancel()
                }
                withContext(NonCancellable) {
                    callback()
                }
            }
        }
        if (selfCancelled) {// if we are cancelled, treat it as `from` is cancelled
            runCancelCallback(true)
        } else {
            when (from.status) {
                Status.RUNNING -> throw Throwable()
                Status.SUCCEED -> perform(context, fixedPromiseF) perform@{
                    if (onFinally != null) {
                        onFinally(
                            @Suppress("UNCHECKED_CAST")
                            (this@perform as ForwardPack<ANOTHER>)
                        )
                    } else {
                        onSucceeded(object : ValuePack<ANOTHER, RESULT>,
                            ResolvePack<RESULT> by this {
                            override val value: ANOTHER
                                get() = @Suppress("UNCHECKED_CAST") (from.value as ANOTHER)
                        })
                    }
                }

                Status.FAILED -> perform(context, fixedPromiseF) perform@{
                    if (onFinally != null) {
                        onFinally(
                            @Suppress("UNCHECKED_CAST")
                            (this@perform as ForwardPack<ANOTHER>)
                        )
                    } else {
                        onFailed(object : ReasonPack<RESULT>, ResolvePack<RESULT> by this {
                            override val reason: Throwable
                                get() = from.reason as Throwable
                        })
                    }
                }

                Status.CANCELLED -> runCancelCallback(false)
            }
        }
    }

    fun <NEXT_RESULT> then(
        onSucceeded: SucceededHandler<RESULT, NEXT_RESULT>,
        config: PromiseConfig?,
    ) =
        Promise(
            (config ?: PromiseConfig.EMPTY_CONFIG).copy(
                shouldWrapJobWithSemaphore = false
            )
        ) next@{
            state().self.transfer(
                true,
                this@Promise,
                onSucceeded = { onSucceeded() }
            )
            waiting()
        }

    fun next(
        onSucceeded: SucceededConsumer<RESULT>,
        config: PromiseConfig?,
    ) = then({
        onSucceeded()
        rsv(null)
    }, config)

    fun <NEXT_RESULT> catch(
        onFailed: FailedHandler<NEXT_RESULT>,
        config: PromiseConfig?,
    ) =
        Promise(
            (config ?: PromiseConfig.EMPTY_CONFIG).copy(
                shouldWrapJobWithSemaphore = false
            )
        ) next@{
            state().self.transfer(
                true,
                this@Promise,
                onFailed = { onFailed() }
            )
            waiting()
        }

    fun recover(
        onFailed: FailedConsumer,
        config: PromiseConfig?,
    ) = catch({
        onFailed()
        rsv(null)
    }, config)

    fun <NEXT_RESULT> forCancel(
        onCancelled: CancelledListener,
        config: PromiseConfig?,
    ) =
        Promise<NEXT_RESULT>(
            (config ?: PromiseConfig.EMPTY_CONFIG).copy(
                shouldWrapJobWithSemaphore = false
            )
        ) next@{
            state().self.transfer(
                true,
                this@Promise,
                onCancelled = { onCancelled() }
            )
            waiting()
        }

    fun aborted(
        onCancelled: CancelledListener,
        config: PromiseConfig?,
    ) = forCancel<RESULT>(onCancelled, config)

    fun terminated(
        onCancelled: CancelledListener,
        config: PromiseConfig?,
    ) = forCancel<Any?>(onCancelled, config)

    fun finally(
        onFinally: FinallyHandler<RESULT>,
        config: PromiseConfig?,
    ) =
        Promise<RESULT>(
            (config ?: PromiseConfig.EMPTY_CONFIG).copy(
                shouldWrapJobWithSemaphore = false
            )
        ) next@{
            state().self.transfer(
                true,
                this@Promise,
                onFinally = { onFinally() }
            )
            waiting()
        }

    fun last(
        onFinally: FinallyConsumer<RESULT>,
        config: PromiseConfig?,
    ) = finally({
        onFinally()
        forward()
    }, config)

    @Suppress("unused")
    fun setTimeoutAsync(
        timeoutSerialNumber: Int,
        d: Duration,
        onTimeOut: suspend (d: Duration) -> Unit = { }
    ): Promise<RESULT> {
        var schedule = false
        setTimeoutMutex.synchronizedAsync(block = block1@{
            if (timeoutTriggered) {
                return@block1
            }
            if (timeoutSerialNumber <= timeoutSN) {
                return@block1
            }
            timeoutSN = timeoutSerialNumber
            schedule = true
        }, after = after1@{
            if (schedule.not()) {
                return@after1
            }
            Promise(null) {
                delay(d)
                var valid: Boolean? = null
                setTimeoutMutex.synchronizedAsync(block = block2@{
                    if (timeoutTriggered) {
                        valid = false
                        return@block2
                    }
                    if (timeoutSerialNumber != timeoutSN) {
                        valid = false
                        return@block2
                    }
                    true.also {
                        timeoutTriggered = it
                        valid = it
                    }
                }, after = after2@{
                    if (!valid!! || !cancel()) {
                        rsv(null)
                        return@after2
                    }
                    onTimeOut(d)
                    rsv(null)
                })
                waiting()
            }
        })
        return this
    }

    suspend fun await(): RESULT {
        awaitSettled()
        return when (status) {
            Status.RUNNING -> throw Throwable()
            Status.SUCCEED -> @Suppress("UNCHECKED_CAST") (value as RESULT)
            Status.FAILED -> throw reason!!
            Status.CANCELLED -> throw kotlin.coroutines.cancellation.CancellationException()
        }
    }

    companion object {
        fun <RESULT> raceN(
            config: PromiseConfig?,
            vararg promises: Promise<RESULT>
        ): Promise<RESULT> {
            return Promise(config) {
                rsp(select {
                    for (p in promises) {
                        p.onSettled { p }
                    }
                })
            }
        }

        fun <RESULT> resolve(value: RESULT) = Promise<RESULT>(
            null,
            PromiseConfig.EMPTY_CONFIG
        ).apply {
            succeed(value)
        }

        fun <RESULT> reject(reason: Throwable) = Promise<RESULT>(
            null,
            PromiseConfig.EMPTY_CONFIG
        ).apply {
            fail(reason)
        }

        fun error(reason: Throwable) = reject<Any?>(reason)

        @Suppress("MemberVisibilityCanBePrivate")
        fun <RESULT> cancel() = Promise<RESULT>(
            null,
            PromiseConfig.EMPTY_CONFIG
        ).apply {
            cancel()
        }

        fun abort() = cancel<Any?>()
    }
}

private fun Job.toPromiseScope(): PromiseScope = run {
    with(object : PromiseCancelledBroadcast {
        override val isActive: Boolean
            get() = this@run.isActive

        override fun listen(r: () -> Unit): PromiseCancelledBroadcastKey {
            return PromiseCancelledBroadcastKey(this@run.invokeOnCompletion {
                if (it is CancellationException) {
                    r()
                }
            })
        }

        override fun unListen(key: PromiseCancelledBroadcastKey) {
            (key.payload as DisposableHandle).dispose()
        }
    }) {
        object : PromiseScope {
            override val scopeCancelledBroadcast: PromiseCancelledBroadcast
                get() = this@with
        }
    }
}

interface Work {
    fun cancel()
}

class Task<RESULT>(
    val promise: Promise<RESULT>,
    private val cancelledBroadcaster: PromiseCancelledBroadcaster?
) : Work {
    override fun cancel() {
        cancelledBroadcaster!!.broadcast()
    }
}

typealias ProcessFunc<RESULT> = PromiseScope.() -> Promise<RESULT>

typealias WorkFunc = PromiseScope.() -> Unit

fun WorkFunc.toProcessFunc(): ProcessFunc<Any?> = {
    this@toProcessFunc()
    Promise.resolve(null)
}

private fun <RESULT> process(
    promiseScope: PromiseScope,
    cancelledBroadcaster: PromiseCancelledBroadcaster?,
    builder: ProcessFunc<RESULT>
): Task<RESULT> = Task(promiseScope.builder(), cancelledBroadcaster)

/**
 * promise.Promise listens to the outer Job, but does not integrate itself into the coroutines' Job tree,
 * in other words, promise.Promise knows about the Job, but the Job doesn't know about promise.Promise
 */
private fun <RESULT> processInNewJob(parentJob: Job? = null, builder: ProcessFunc<RESULT>) =
    Job(parentJob).run newJob@{
        toPromiseScope().run scope@{
            process(
                this@scope,
                object :
                    PromiseCancelledBroadcaster,
                    PromiseCancelledBroadcast by this@scope.scopeCancelledBroadcast!! {
                    override fun broadcast() {
                        this@newJob.cancel()
                    }
                },
                builder
            )
        }
    }

//
fun <RESULT> process(builder: ProcessFunc<RESULT>) = processInNewJob(builder = builder)
fun work(builder: WorkFunc) = process(builder.toProcessFunc())
fun <RESULT> promise(job: PromiseJob<RESULT>) = process { promise { job() } }
fun trigger(job: PromiseJob<Any?>) = promise(job)

//
fun <RESULT> PromiseScope.process(builder: ProcessFunc<RESULT>) =
    PromiseCancelledBroadcaster.new().let { broadcaster ->
        PromiseCancelledBroadcast.merge(scopeCancelledBroadcast, broadcaster).let { broadcast ->
            object : PromiseScope {
                override val scopeCancelledBroadcast: PromiseCancelledBroadcast
                    get() = broadcast
            }.let { scope ->
                process(
                    scope,
                    broadcaster,
                    builder
                )
            }
        }
    }

fun PromiseScope.work(builder: WorkFunc) = process(builder.toProcessFunc())

//
fun <RESULT> CoroutineScope.process(builder: ProcessFunc<RESULT>) = processInNewJob(
    coroutineContext[Job], builder
)

fun CoroutineScope.work(builder: WorkFunc) = process(builder.toProcessFunc())
fun <RESULT> CoroutineScope.promise(job: PromiseJob<RESULT>) = process { promise { job() } }

@Suppress("unused")
fun CoroutineScope.trigger(job: PromiseJob<Any?>) = promise(job)