package http

import WEBSOCKET_PING_INTERVAL_SECONDS
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.discardRemaining
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.charset
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.parameters
import io.ktor.http.withCharsetIfNeeded
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charsets
import kotlinx.serialization.json.Json
import promise.PromiseScope
import promise.task.TaskOnce
import toString
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val httpClient = httpClientBase.config {
    install(HttpCookies) {
        httpCookieManager?.also {
            storage = it
        }
    }
    install(HttpTimeout)
    install(WebSockets) {
        pingInterval = WEBSOCKET_PING_INTERVAL_SECONDS * 1000L
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
}

private val httpClientFollow = httpClient.config {
    followRedirects = true
}

private val httpClientNoFollow = httpClient.config {
    followRedirects = false
}

data class HTTPRequest(
    val method: HTTPMethod = HTTPMethod.GET,
    val url: String? = null,
    val uri: Url? = null,
    val customizedHeaderList: List<Array<String>>? = null,
    @Suppress("ArrayInDataClass")
    val requestBinary: ByteArray? = null,
    val requestForm: List<Array<String>>? = null,
    val requestString: String? = null,
    val requestContentType: ContentType? = null,
    val connectTimeout: Duration = 2.seconds,
    val readTimeout: Duration = 20.seconds,
    val writeTimeout: Duration = 20.seconds,
    val timeout: Duration = connectTimeout + readTimeout + writeTimeout,
    val isQuickTest: Boolean = false,
    val followRedirect: Boolean = true,
) {
    @Suppress("MemberVisibilityCanBePrivate")
    var status: HttpStatusCode = HttpStatusCode(0, "")

    @Suppress("MemberVisibilityCanBePrivate")
    var statusCode: Int = 0

    @Suppress("MemberVisibilityCanBePrivate")
    var statusMessage: String = ""

    @Suppress("MemberVisibilityCanBePrivate")
    var responseHeaders: Headers = Headers.Empty

    //
    private var response: HttpResponse? = null

    //
    private var scope: PromiseScope? = null
    private var sendWithoutClose: TaskOnce<HTTPResult<HTTPRequest>>? = null
    private var send: TaskOnce<HTTPResult<HTTPRequest>>? = null
    private var byteArray: TaskOnce<HTTPResult<ByteArray>>? = null
    private var string: TaskOnce<HTTPResult<String>>? = null
    private var htmlDocument: TaskOnce<HTTPResult<Document>>? = null

    fun prepare(scope: PromiseScope): HTTPRequest {
        this.scope = scope
        sendWithoutClose = TaskOnce(scope, null)
        send = TaskOnce(scope, null)
        byteArray = TaskOnce(scope, null)
        string = TaskOnce(scope, null)
        htmlDocument = TaskOnce(scope, null)
        return this
    }

    private fun sendWithoutClose() = sendWithoutClose!!.perform(scope!!) {
        promise {
            val builder: HttpRequestBuilder.() -> Unit = {
                method = HttpMethod(this@HTTPRequest.method.method)
                uri?.also {
                    url(it)
                } ?: url(this@HTTPRequest.url)
                customizedHeaderList?.also {
                    headers {
                        it.forEach { (k, v) ->
                            append(k, v)
                        }
                    }
                }
                timeout {
                    connectTimeoutMillis = connectTimeout.inWholeMilliseconds
                    socketTimeoutMillis = max(
                        readTimeout.inWholeMilliseconds,
                        writeTimeout.inWholeMilliseconds
                    )
                    requestTimeoutMillis = timeout.inWholeMilliseconds
                }
            }
            val httpClient = if (followRedirect) httpClientFollow else httpClientNoFollow
            response = requestForm?.let {
                httpClient.submitForm(
                    formParameters = parameters {
                        it.forEach { (k, v) ->
                            append(k, v)
                        }
                    },
                    encodeInQuery = false,
                    block = builder
                )
            } ?: requestString?.let {
                httpClient.request {
                    contentType(
                        requestContentType ?: ContentType
                            .Text
                            .Plain
                            .withCharsetIfNeeded(Charsets.UTF_8)
                    )
                    setBody(it)
                    builder()
                }
            } ?: requestBinary?.let {
                httpClient.request {
                    requestContentType?.also {
                        contentType(it)
                    }
                    setBody(ByteReadChannel(it))
                    builder()
                }
            } ?: httpClient.request(builder)
            response!!.also {
                @Suppress("NestedLambdaShadowedImplicitParameter")
                status = it.status.also {
                    statusCode = it.value
                    statusMessage = it.description
                }
                responseHeaders = it.headers
            }
            rsv(HTTPResult(this@HTTPRequest, this@HTTPRequest))
        }
    }

    @Suppress("unused")
    fun send() = send!!.perform(scope!!) {
        sendWithoutClose().finally {
            response?.discardRemaining()
            forward()
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun byteArray() = byteArray!!.perform(scope!!) {
        sendWithoutClose().then {
            rsv(
                HTTPResult(
                    value.request,
                    value.request.response!!.body()
                )
            )
        }
    }

    fun string() = string!!.perform(scope!!) {
        byteArray().then {
            rsv(
                HTTPResult(
                    value.request,
                    value.result.toString(
                        value.request.response!!.charset() ?: Charsets.UTF_8
                    )
                )
            )
        }
    }

    @Suppress("unused")
    fun htmlDocument() = htmlDocument!!.perform(scope!!) {
        string().then {
            rsv(
                HTTPResult(
                    value.request,
                    Ksoup.parse(value.result)
                )
            )
        }
    }
}