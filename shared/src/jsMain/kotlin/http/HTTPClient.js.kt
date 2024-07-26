package http

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

internal actual val httpClientBase = HttpClient(Js)