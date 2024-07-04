package promise

import kotlinx.coroutines.channels.Channel

typealias PromiseCancelledBroadcastChan = Channel<Any?>

class PromiseCancelledBroadcastKey private constructor(
    val payload: Any?,
    private val innerList: List<PromiseCancelledBroadcastKey?>?,
    private val innerListenChan: PromiseCancelledBroadcastChan?,
    private val innerUnListenChan: PromiseCancelledBroadcastChan?,
) {
    val list get() = innerList!!
    val listenChan get() = innerListenChan!!
    val unListenChan get() = innerUnListenChan!!

    constructor(list: List<PromiseCancelledBroadcastKey?>, payload: Any? = null) : this(
        payload = payload,
        innerList = list,
        innerListenChan = null,
        innerUnListenChan = null,
    )

    constructor(
        payload: Any? = null,
    ) : this(
        payload = payload,
        innerList = null,
        innerListenChan = Channel(),
        innerUnListenChan = Channel(),
    )
}

interface PromiseCancelledBroadcast {
    val isActive: Boolean
    fun listen(r: () -> Unit): PromiseCancelledBroadcastKey
    fun unListen(key: PromiseCancelledBroadcastKey)

    companion object {
        fun merge(
            vararg broadcasts: PromiseCancelledBroadcast?
        ): PromiseCancelledBroadcast = object : PromiseCancelledBroadcast {
            override val isActive: Boolean
                get() {
                    var res = true
                    for (broadcast in broadcasts) {
                        res = res && (broadcast?.isActive ?: true)
                    }
                    return res
                }

            override fun listen(r: () -> Unit): PromiseCancelledBroadcastKey {
                val once = Channel<Any?>(1)
                val onceR = onceRun@{
                    if (once.trySend(null).isSuccess.not()) {
                        return@onceRun
                    }
                    r()
                }
                return broadcasts
                    .map { it?.listen(onceR) }
                    .toList()
                    .let { PromiseCancelledBroadcastKey(it) }
            }

            override fun unListen(key: PromiseCancelledBroadcastKey) {
                for ((i, k) in key.list.withIndex()) {
                    k?.let { broadcasts[i]?.unListen(it) }
                }
            }
        }
    }
}