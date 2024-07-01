import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

interface JS {
    suspend fun hello()
    suspend fun world(): String
    suspend fun version(): Int
    suspend fun ms(money: Money): String
}

typealias JSNothing = Boolean?

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSI {
    fun hello(res: (JSNothing) -> Unit)
    fun world(res: (String) -> Unit)
    fun version(res: (Int) -> Unit)
    fun ms(moneyStr: String, res: (String) -> Unit)
}
