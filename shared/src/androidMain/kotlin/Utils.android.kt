import io.ktor.utils.io.charsets.Charset

actual fun ByteArray.toString(charset: Charset): String {
    return String(this, charset)
}