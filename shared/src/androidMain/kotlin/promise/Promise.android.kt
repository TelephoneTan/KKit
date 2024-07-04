package promise

import kotlinx.coroutines.runBlocking

@Suppress("unused")
fun <RESULT> Task<RESULT>.await() = runBlocking { promise.await() }