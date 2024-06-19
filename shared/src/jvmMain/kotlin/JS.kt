import JSZipline.Companion.jsZipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.loader.FreshnessChecker
import app.cash.zipline.loader.LoadResult
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineLoader
import kotlinx.coroutines.asCoroutineDispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.Executors

private val ziplineLoader = ZiplineLoader(
    Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
    ManifestVerifier.NO_SIGNATURE_CHECKS,
    OkHttpClient()
)

actual suspend fun js(): JS = when (val result = ziplineLoader.loadOnce(
    JSZipline.NAME,
    object : FreshnessChecker {
        override fun isFresh(manifest: ZiplineManifest, freshAtEpochMs: Long): Boolean {
            return isZiplineJSFresh(manifest.version)
        }
    },
    "${HTTPBases.SERVER}${URLSuffixes.ZIPLINE_JS}"
)) {
    is LoadResult.Success -> result.zipline
    is LoadResult.Failure -> throw result.exception
}.jsZipline