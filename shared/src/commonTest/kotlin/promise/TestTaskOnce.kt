package promise

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import log
import promise.task.TaskOnce
import time
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class TestTaskOnce {
    @Test
    fun test() {
        log("hello")
        trigger {
            promise.trigger {
                val task = TaskOnce(promiseScope) {
                    promise {
                        delay(1.seconds)
                        val n = Random.nextInt(4)
                        "$time world $n".let {
                            log(it)
                            if (n != 2) {
                                throw Throwable()
                            }
                            rsv(it)
                        }
                    }
                }
                for (x in 1..10) {
                    @OptIn(DelicateCoroutinesApi::class)
                    GlobalScope.launch {
                        task.perform(PromiseScope).next({
                            log("receive: $value")
                        }, null)
                    }
                }
                waiting()
            }.also {
                delay(5.seconds)
                it.cancel()
            }
            waiting()
        }.testAwait()
    }
}