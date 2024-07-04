package promise

interface PromiseScope {
    val scopeCancelledBroadcast: PromiseCancelledBroadcast?
    fun <RESULT> promise(
        config: PromiseConfig? = null,
        job: PromiseJob<RESULT>,
    ) = Promise(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), job
    )

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun <RESULT, NEXT_RESULT> Promise<RESULT>.then(
        config: PromiseConfig? = null,
        onSucceeded: SucceededHandler<RESULT, NEXT_RESULT>
    ) = then(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), onSucceeded
    )

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun <RESULT> Promise<RESULT>.next(
        config: PromiseConfig? = null,
        onSucceeded: SucceededConsumer<RESULT>
    ) = next(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), onSucceeded
    )

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun <NEXT_RESULT> Promise<*>.catch(
        config: PromiseConfig? = null,
        onFailed: FailedHandler<NEXT_RESULT>
    ) = catch(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), onFailed
    )

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun Promise<*>.recover(
        config: PromiseConfig? = null,
        onFailed: FailedConsumer
    ) = recover(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), onFailed
    )

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun <NEXT_RESULT> Promise<*>.forCancel(
        config: PromiseConfig? = null,
        onCancelled: CancelledListener
    ) = forCancel<NEXT_RESULT>(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), onCancelled
    )

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun Promise<*>.aborted(
        config: PromiseConfig? = null,
        onCancelled: CancelledListener
    ) = aborted(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), onCancelled
    )

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun Promise<*>.terminated(
        config: PromiseConfig? = null,
        onCancelled: CancelledListener
    ) = terminated(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), onCancelled
    )

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun <RESULT> Promise<RESULT>.finally(
        config: PromiseConfig? = null,
        onFinally: FinallyHandler<RESULT>
    ) = finally(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), onFinally
    )

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    fun <RESULT> Promise<RESULT>.last(
        config: PromiseConfig? = null,
        onFinally: FinallyConsumer<RESULT>
    ) = last(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), onFinally
    )

    fun <RESULT> race(
        config: PromiseConfig? = null,
        vararg promises: Promise<RESULT>
    ) = Promise.race(
        (config ?: PromiseConfig.EMPTY_CONFIG).copy(
            scopeCancelledBroadcast = scopeCancelledBroadcast
        ), *promises
    )

    fun <RESULT> race(
        vararg promises: Promise<RESULT>
    ) = race(null, *promises)
}