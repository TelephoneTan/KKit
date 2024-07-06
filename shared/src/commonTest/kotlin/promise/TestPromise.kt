package promise

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import log.log
import log.logBlock
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class TestPromise {
    @Test
    fun test() {
        try {
            process {
                log("hello world")
                //
                promise {
                    delay(2.seconds)
                    rsv(666)
                }.next {
                    try {
                        withTimeout(2.seconds) {
                            try {
                                delay(10.seconds)
                            } catch (e: Throwable) {
                                logBlock("throwable within withTimeout") {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        throw e
                    }
                    rsv("echo $value to the world")
                }.next {
                    log("$value")
                    rsv(Unit)
                }.next {
                    delay(4.seconds)
                    throw Throwable()
                    @Suppress("UNREACHABLE_CODE")
                    rsv("say hello to the world")
                }.catch<Any?> {
                    try {
                        delay(10.seconds)
                    } catch (e: Throwable) {
                        logBlock("throwable from catch 1 delay") {
                            e.printStackTrace()
                        }
                    }
                    logBlock("caught 1") {
                        reason.printStackTrace()
                    }
                    delay(2.seconds)
                    log("catch 1 after delay")
                    throw IllegalStateException()
                }.setTimeoutAsync(1, 2.seconds) {
                    log("time out! $it")
                    delay(2.seconds)
                    log("time out! $it after delay")
                }.setTimeoutAsync(2, 80.5.seconds) {
                    log("time out! $it")
                    delay(2.seconds)
                    log("time out! $it after delay")
                }.forCancel<Any?> {
                    log("cancelled here 1")
                    try {
                        delay(2.seconds)
                    } catch (e: Throwable) {
                        logBlock("throwable from cancel 1 delay") {
                            e.printStackTrace()
                        }
                    }
                    log("cancelled here 1 after delay")
                }.catch<Any?> {
                    log("caught 2 $reason")
                    throw reason
                }.finally {
                    try {
                        delay(10.seconds)
                    } catch (e: Throwable) {
                        logBlock("throwable from finally delay") {
                            e.printStackTrace()
                        }
                    }
                    log("finally here $isActive")
                    rsp(promise {
                        delay(1.seconds)
                        rsp(promise {
                            log("调用A")
                            delay(1.seconds)
                            log("调用B")
                            rsp(Promise.abort())
                            rej(kotlin.coroutines.cancellation.CancellationException())
                        })
                    })
                }.then {
                    rsv(value as String)
                }.next {
                    log("say $value to the world ${value.length}")
                    rsv(null)
                }.catch {
                    log("caught 3 $reason")
                    rsv("bbb")
                }.next {
                    log("say $value to the world ${value.length}")
                    rsv(null)
                }.forCancel<Any?> {
                    log("cancelled here 2")
                    try {
                        delay(2.seconds)
                    } catch (e: Throwable) {
                        logBlock("throwable from cancel 2 delay") {
                            e.printStackTrace()
                        }
                    }
                    log("cancelled here 2 after delay")
                }
            }.also {
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    delay(9.seconds)
                    it.cancel()
                }
            }.testAwait()
        } catch (e: CancellationException) {
            log("await cancel caught $e")
        }
        log("await pass")
        promise {
            delay(600.seconds)
            rsv(null)
        }.testAwait()
    }
}