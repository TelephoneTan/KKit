import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineLoader
import kotlinx.coroutines.asCoroutineDispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.Executors

suspend fun jsi(): JS = jsi(
    ZiplineLoader(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
        ManifestVerifier.NO_SIGNATURE_CHECKS,
        OkHttpClient()
    )
)