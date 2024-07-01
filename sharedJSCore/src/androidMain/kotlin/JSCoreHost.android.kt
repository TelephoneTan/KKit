import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

suspend fun jsCore() = jsCore({
    Executors.newSingleThreadExecutor {
        Thread(null, it, jsCoreBrand)
    }.asCoroutineDispatcher()
}) {
    HttpClient(CIO).get(it).body()
}