package promise

import kotlinx.coroutines.channels.Channel
import kotlin.concurrent.Volatile

class PromiseSemaphore(n: Long) {
    @Volatile
    private var parent: PromiseSemaphore? = null
    private val ticketChannel: Channel<Unit> = Channel(Channel.UNLIMITED)
    private val darkChannel: Channel<Unit> = Channel(Channel.UNLIMITED)

    init {
        increase(n)
    }

    suspend fun acquire(n: ULong = 1UL) {
        var s: PromiseSemaphore? = this
        while (s != null) {
            for (i in 1UL..n) {
                s.ticketChannel.receive()
            }
            s = s.parent
        }
    }

    @Suppress("unused")
    suspend fun acquire(n: Long) {
        acquire(n.toULong())
    }

    fun release(n: ULong = 1UL) {
        var s: PromiseSemaphore? = this
        while (s != null) {
            s.increase(n)
            s = s.parent
        }
    }

    fun release(n: Long) {
        release(n.toULong())
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun increase(n: ULong) {
        for (i in 1UL..n) {
            if (darkChannel.tryReceive().isSuccess) {
                continue
            }
            ticketChannel.trySend(Unit)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun increase(n: Long) {
        increase(n.toULong())
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun reduce(n: ULong) {
        for (i in 1UL..n) {
            darkChannel.trySend(Unit)
        }
    }

    @Suppress("unused")
    fun reduce(n: Long) {
        reduce(n.toULong())
    }

    @Suppress("unused")
    fun include(vararg children: PromiseSemaphore?): PromiseSemaphore {
        for (child in children) {
            child?.parent = this
        }
        return this
    }
}