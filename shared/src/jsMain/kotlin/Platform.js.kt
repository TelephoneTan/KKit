import kotlinx.browser.window

class JSPlatform : Platform {
    override val name: String =
        "JS in ${
            when {
                isNodeJS -> globalThis.JSON.stringify(globalThis.process.versions, null, "  ")
                isBrowser -> window.navigator.userAgent
                else -> "Unknown JS Runtime"
            }
        }"
}

actual fun getPlatform(): Platform = JSPlatform()