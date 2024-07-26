import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.name
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.CoreFoundation.CFStringConvertEncodingToNSStringEncoding
import platform.CoreFoundation.CFStringConvertIANACharSetNameToEncoding
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFStringEncodingInvalidId
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.NSString
import platform.Foundation.NSStringEncoding
import platform.Foundation.create

private fun Charset.toNSStringEncoding(): NSStringEncoding {
    @OptIn(ExperimentalForeignApi::class)
    return memScoped {
        CFStringConvertEncodingToNSStringEncoding(
            CFStringConvertIANACharSetNameToEncoding(
                CFStringCreateWithCString(
                    null,
                    name,
                    kCFStringEncodingUTF8
                )
            ).takeIf { it != kCFStringEncodingInvalidId }!!
        )
    }
}

actual fun ByteArray.toString(charset: Charset): String {
    return this.let {
        @OptIn(ExperimentalForeignApi::class)
        memScoped {
            @OptIn(BetaInteropApi::class)
            NSString.create(
                bytes = it.refTo(0).getPointer(this),
                length = it.size.toULong(),
                encoding = charset.toNSStringEncoding()
            )!!.toString()
        }
    }
}