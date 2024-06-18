import kotlinx.browser.window

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm in ${window.navigator.userAgent}"
}

actual fun getPlatform(): Platform = WasmPlatform()