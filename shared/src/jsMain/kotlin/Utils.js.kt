import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.name
import org.khronos.webgl.Uint8Array

external val globalThis: dynamic
val isNodeJS: Boolean get() = globalThis.process != null
val isBrowser: Boolean get() = globalThis.self != null

actual fun ByteArray.toString(charset: Charset): String {
    return globalThis
        .TextDecoder(charset.name)
        .decode(Uint8Array(toTypedArray())) as String
}