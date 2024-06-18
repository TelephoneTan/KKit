import kotlinx.browser.window

external val globalThis: dynamic

class JSPlatform : Platform {
    override val name: String =
        "JS in ${
            when {
                isNodeJS -> globalThis.JSON.stringify(globalThis.process.versions, null, "  ")
                isBrowser -> window.navigator.userAgent
                else -> throw Throwable()
            }
        }"
}

actual fun getPlatform(): Platform = JSPlatform()