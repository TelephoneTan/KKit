import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Money private constructor(val symbol: String) {
    companion object {
        val Dollar = Money("$")
        val RMB = Money("¥")
    }
}