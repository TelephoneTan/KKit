import JSZipline.Companion.jsZipline
import KKit.shared.BuildConfig
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.loader.FreshnessChecker
import app.cash.zipline.loader.LoadResult
import app.cash.zipline.loader.ZiplineLoader

internal suspend fun jsi(loader: ZiplineLoader): JS = when (val result = loader.loadOnce(
    JSZipline.NAME,
    object : FreshnessChecker {
        override fun isFresh(manifest: ZiplineManifest, freshAtEpochMs: Long): Boolean {
            return isZiplineJSFresh(manifest.version)
        }
    },
    "${HTTPBases.SERVER}${URLSuffixes.ziplineJS(BuildConfig.ZIPLINE_JS_VERSION)}"
)) {
    is LoadResult.Success -> result.zipline
    is LoadResult.Failure -> throw result.exception
}.jsZipline