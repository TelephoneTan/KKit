package promise

import kotlinx.coroutines.sync.Mutex
import synchronizedAsync
import kotlin.concurrent.Volatile

interface PromiseCancelledBroadcaster : PromiseCancelledBroadcast {
    fun broadcast()

    companion object {
        fun new(): PromiseCancelledBroadcaster = object : PromiseCancelledBroadcaster {
            private val mutex = Mutex()
            private val listeners = mutableMapOf<PromiseCancelledBroadcastKey, () -> Unit>()

            override fun broadcast() {
                val todos = mutableMapOf<PromiseCancelledBroadcastKey, () -> Unit>()
                mutex.synchronizedAsync(block = {
                    if (isActive.not()) {
                        return@synchronizedAsync
                    }
                    isActive = false
                    todos.putAll(listeners)
                    listeners.clear()
                }, after = {
                    for ((key, todo) in todos) {
                        if (key.unListenChan.tryReceive().isClosed) {
                            continue
                        }
                        try {
                            todo()
                        } catch (_: Throwable) {
                        }
                    }
                })
            }

            @Volatile
            override var isActive = true

            override fun listen(r: () -> Unit): PromiseCancelledBroadcastKey {
                val key = PromiseCancelledBroadcastKey()
                var callback: (() -> Unit)? = null
                mutex.synchronizedAsync(block = {
                    if (key.unListenChan.tryReceive().isClosed) {
                        return@synchronizedAsync
                    }
                    if (isActive) {
                        listeners[key] = r
                    } else {
                        callback = r
                    }
                    key.listenChan.close()
                }, after = {
                    callback?.invoke()
                })
                return key
            }

            override fun unListen(key: PromiseCancelledBroadcastKey) {
                key.unListenChan.close()
                mutex.synchronizedAsync(before = {
                    key.listenChan.receiveCatching()
                }, block = {
                    listeners.remove(key)
                })
            }
        }
    }
}