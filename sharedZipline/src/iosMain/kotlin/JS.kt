import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineCache
import app.cash.zipline.loader.ZiplineLoader
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import okio.FileSystem
import platform.Foundation.NSURLSession

private val loader =
    @OptIn(ExperimentalCoroutinesApi::class)
    ZiplineLoader(
        @OptIn(DelicateCoroutinesApi::class)
        newSingleThreadContext("ZiplineJS"),
        ManifestVerifier.NO_SIGNATURE_CHECKS,
        NSURLSession.sharedSession
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

suspend fun jsi(): JS = jsi(loader, fetchLatestZiplineVersion())