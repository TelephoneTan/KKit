internal suspend fun fetchLatestZiplineVersion(fetcher: suspend (String) -> Int): Int = fetcher(
    "${HTTPBases.SERVER}${URLSuffixes.ZIPLINE_JS_VERSION_LATEST}"
)

internal fun isZiplineJSFresh(cachedVersion: String?, requiredVersion: Int) =
    cachedVersion?.toInt()?.let { it == requiredVersion } ?: false