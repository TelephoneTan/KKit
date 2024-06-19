import KKit.shared.BuildConfig

class JSZiplineImpl : JSZipline {
    override fun hello() {
        console.log("hello, ${world()}")
    }

    override fun world(): String {
        return "Zipline"
    }

    override fun version(): Int {
        return BuildConfig.ZIPLINE_JS_VERSION
    }
}