import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext

suspend fun jsCore() = jsCore({
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    newSingleThreadContext(jsCoreBrand)
}) {
    HttpClient(CIO).get(it).body()
}