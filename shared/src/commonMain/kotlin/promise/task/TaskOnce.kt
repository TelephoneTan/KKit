package promise.task

import kotlinx.coroutines.channels.Channel
import promise.Promise
import promise.PromiseScope
import promise.Status

typealias TaskOnceJob<RESULT> = PromiseScope.() -> Promise<RESULT>

data class TaskOnceReq<RESULT>(
    val job: TaskOnceJob<RESULT>? = null,
    val result: Channel<Promise<RESULT>> = Channel(1),
)

class TaskOnce<RESULT>(
    scope: PromiseScope,
    @Suppress("MemberVisibilityCanBePrivate")
    val job: TaskOnceJob<RESULT>?
) {
    private val reqChan = Channel<TaskOnceReq<RESULT>>()

    init {
        scope.trigger {
            var current: Promise<RESULT>? = null
            for (req in reqChan) {
                while (current == null) {
                    (req.job ?: job)!!.invoke(promiseScope).also { tp ->
                        tp.awaitSettled()
                        tp.state.takeIf { it == Status.SUCCEED }?.let {
                            current = tp
                        }
                    }
                }
                req.result.trySend(current!!)
            }
            waiting()
        }
    }

    fun perform(scope: PromiseScope, job: TaskOnceJob<RESULT>?): Promise<RESULT> {
        return scope.promise {
            rsp(TaskOnceReq(job = job).let {
                reqChan.send(it)
                it.result.receive()
            })
        }
    }
}