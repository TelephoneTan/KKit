package http

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual val httpClientBase = HttpClient(Darwin)