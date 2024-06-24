import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineCache
import app.cash.zipline.loader.ZiplineLoader
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.asCoroutineDispatcher
import okhttp3.OkHttpClient
import okio.FileSystem
import java.util.concurrent.Executors

private val loader = ZiplineLoader(
    Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
    ManifestVerifier.NO_SIGNATURE_CHECKS,
    OkHttpClient()
).withCache(
    ZiplineCache(
        FileSystem.SYSTEM,
        FileSystem.SYSTEM_TEMPORARY_DIRECTORY,
        Long.MAX_VALUE
    )
)

suspend fun fetchLatestZiplineVersion(): Int = fetchLatestZiplineVersion {
    HttpClient(CIO).get(it).body()
}

suspend fun jsi() = jsi(loader, fetchLatestZiplineVersion())