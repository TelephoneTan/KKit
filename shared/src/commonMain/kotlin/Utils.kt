import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

fun String?.nonEmptyOrNull() = this?.takeIf { it.isNotEmpty() }

val json = Json {
    encodeDefaults = true
    explicitNulls = true
    ignoreUnknownKeys = true
}

@OptIn(DelicateCoroutinesApi::class)
fun Mutex.synchronizedAsync(
    before: suspend () -> Unit = {},
    block: () -> Unit,
    after: suspend () -> Unit,
) = GlobalScope.launch(Dispatchers.Default) {
    before()
    withLock {
        block()
    }
    after()
}

@Suppress("unused")
fun Mutex.synchronizedAsync(
    @Suppress("UNUSED_PARAMETER") nothing: Any? = null,
    before: suspend () -> Unit,
    block: () -> Unit,
) = synchronizedAsync(before = before, block = block, after = {})

@Suppress("unused")
fun Mutex.synchronizedAsync(
    block: () -> Unit,
) = synchronizedAsync(before = {}, block = block, after = {})