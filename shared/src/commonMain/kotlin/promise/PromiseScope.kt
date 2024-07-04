package promise

interface PromiseScope {
    val scopeCancelledBroadcast: PromiseCancelledBroadcast?
    val promiseScope get() = this
    fun <RESULT> promise(
        config: PromiseConfig? = null,
        job: PromiseJob<RESULT>,
    ) = Promise(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), job
    )

    fun trigger(
        config: PromiseConfig? = null,
        job: PromiseJob<Any?>,
    ) = Promise(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), job
    )

    fun <RESULT, NEXT_RESULT> Promise<RESULT>.then(
        config: PromiseConfig? = null,
        onSucceeded: SucceededHandler<RESULT, NEXT_RESULT>
    ) = then(
        onSucceeded,
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ),
    )

    fun <RESULT> Promise<RESULT>.next(
        config: PromiseConfig? = null,
        onSucceeded: SucceededConsumer<RESULT>
    ) = next(
        onSucceeded,
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ),
    )

    fun <NEXT_RESULT> Promise<*>.catch(
        config: PromiseConfig? = null,
        onFailed: FailedHandler<NEXT_RESULT>
    ) = catch(
        onFailed,
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ),
    )

    fun Promise<*>.recover(
        config: PromiseConfig? = null,
        onFailed: FailedConsumer
    ) = recover(
        onFailed,
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ),
    )

    fun <NEXT_RESULT> Promise<*>.forCancel(
        config: PromiseConfig? = null,
        onCancelled: CancelledListener
    ) = forCancel<NEXT_RESULT>(
        onCancelled,
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ),
    )

    fun Promise<*>.aborted(
        config: PromiseConfig? = null,
        onCancelled: CancelledListener
    ) = aborted(
        onCancelled,
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ),
    )

    fun Promise<*>.terminated(
        config: PromiseConfig? = null,
        onCancelled: CancelledListener
    ) = terminated(
        onCancelled,
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ),
    )

    fun <RESULT> Promise<RESULT>.finally(
        config: PromiseConfig? = null,
        onFinally: FinallyHandler<RESULT>
    ) = finally(
        onFinally,
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ),
    )

    fun <RESULT> Promise<RESULT>.last(
        config: PromiseConfig? = null,
        onFinally: FinallyConsumer<RESULT>
    ) = last(
        onFinally,
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ),
    )

    fun <RESULT> race(
        config: PromiseConfig? = null,
        vararg promises: Promise<RESULT>
    ) = Promise.raceN(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), *promises
    )

    fun <RESULT> race(
        vararg promises: Promise<RESULT>
    ) = race(null, *promises)

    companion object : PromiseScope {
        override val scopeCancelledBroadcast: PromiseCancelledBroadcast?
            get() = null
    }
}