package promise

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import log
import time
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class TestTaskShared {
    @Test
    fun test() {
        log("hello")
        trigger {
            promise.trigger {
                val task = taskShared {
                    promise {
                        delay(1.2.seconds)
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
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    for (y in 1..10) {
                        for (x in 1..10) {
                            trigger {
                                val name = "($y, $x)"
                                if (x > 5) {
                                    delay(0.5.seconds)
                                }
                                log("$name start")
                                rsp(
                                    task.perform().next({
                                        log("$name receive: $value")
                                    }, null).recover {
                                        log("$name fail: $reason")
                                    }
                                )
                            }
                        }
                        delay(0.5.seconds)
                    }
                }
                waiting()
            }.also {
                delay(6.seconds)
                it.cancel()
            }
            waiting()
        }.testAwait()
    }
}