import KKit.shared.BuildConfig

class JSZiplineImpl : JSZipline {
    override fun hello() {
        console.log("hello, ${world()}")
    }

    override fun world(): String {
        return "Powered by ZiplineJS Canary ${BuildConfig.ZIPLINE_JS_VERSION} ${getPlatform().name}"
    }

    override fun version(): Int {
        return BuildConfig.ZIPLINE_JS_VERSION
    }
}