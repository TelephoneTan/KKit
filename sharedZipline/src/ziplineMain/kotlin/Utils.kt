import KKit.shared.BuildConfig

suspend fun fetchLatestZiplineVersion(): Int = BuildConfig.ZIPLINE_JS_VERSION
internal fun isZiplineJSFresh(cachedVersion: String?) = false