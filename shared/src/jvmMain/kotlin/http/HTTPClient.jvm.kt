package http

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual val httpClientBase = HttpClient(OkHttp)