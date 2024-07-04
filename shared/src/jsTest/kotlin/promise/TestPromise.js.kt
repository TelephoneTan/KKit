package promise

internal actual fun <RESULT> Task<RESULT>.testAwait(): RESULT {
    throw Throwable()
}