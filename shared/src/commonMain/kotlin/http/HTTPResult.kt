package http

data class HTTPResult<E>(
    val request: HTTPRequest,
    val result: E,
)