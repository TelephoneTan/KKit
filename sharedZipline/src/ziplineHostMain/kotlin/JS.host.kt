import JSZipline.Companion.jsZipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.loader.FreshnessChecker
import app.cash.zipline.loader.LoadResult
import app.cash.zipline.loader.ZiplineLoader

internal suspend fun jsi(loader: ZiplineLoader, latestVersion: Int): JS =
    when (val result = loader.loadOnce(
        JSZipline.NAME,
        object : FreshnessChecker {
            override fun isFresh(manifest: ZiplineManifest, freshAtEpochMs: Long): Boolean {
                return isZiplineJSFresh(manifest.version, latestVersion)
            }
        },
        "${HTTPBases.SERVER}${URLSuffixes.ziplineJS(latestVersion)}"
    )) {
        is LoadResult.Success -> result.zipline
        is LoadResult.Failure -> throw result.exception
    }.jsZipline