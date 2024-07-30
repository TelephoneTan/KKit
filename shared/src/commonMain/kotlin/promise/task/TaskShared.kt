package promise.task

import kotlinx.coroutines.channels.Channel
import promise.Promise
import promise.PromiseScope

typealias TaskSharedJob<RESULT> = PromiseScope.() -> Promise<RESULT>

data class TaskSharedReq<RESULT>(
    val job: TaskSharedJob<RESULT>? = null,
    val result: Channel<Promise<RESULT>> = Channel(1),
)

class TaskShared<RESULT>(
    scope: PromiseScope,
    @Suppress("MemberVisibilityCanBePrivate")
    val job: TaskSharedJob<RESULT>?
) {
    private val reqChan = Channel<TaskSharedReq<RESULT>>()

    init {
        scope.trigger {
            while (true) {
                var req = reqChan.receive()
                val current = (req.job ?: job)!!.invoke(promiseScope).also { tp ->
                    tp.awaitSettled()
                }
                req.result.trySend(current)
                while (reqChan.tryReceive().getOrNull()?.also { req = it } != null) {
                    req.result.trySend(current)
                }
            }
            @Suppress("UNREACHABLE_CODE")
            waiting()
        }
    }

    fun perform(scope: PromiseScope, job: TaskSharedJob<RESULT>? = null): Promise<RESULT> {
        return scope.promise {
            rsp(TaskSharedReq(job = job).let {
                reqChan.send(it)
                it.result.receive()
            })
        }
    }
}