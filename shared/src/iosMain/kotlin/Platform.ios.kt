import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String =
        "${UIDevice.currentDevice.systemName()} " +
                "${UIDevice.currentDevice.systemVersion}\n" +
                @OptIn(ExperimentalForeignApi::class)
                Hello.hello("iOS")
}

actual fun getPlatform(): Platform = IOSPlatform()