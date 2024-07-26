import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <reified T> T.use(crossinline block: (T) -> Unit) =
    withContext(Dispatchers.Main) {
        block(this@use)
    }