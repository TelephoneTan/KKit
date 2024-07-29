package http

import WEBSOCKET_PING_INTERVAL_SECONDS
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal actual val httpClientBase = HttpClient(OkHttp) {
    engine {
        preconfigured = OkHttpClient.Builder()
            .pingInterval(WEBSOCKET_PING_INTERVAL_SECONDS.toLong(), TimeUnit.SECONDS)
            .build()
    }
}