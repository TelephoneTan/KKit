package pub.telephone.kkit

import Greeting
import Hosts
import KKit.shared.BuildConfig
import Paths
import Ports
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.request.contentType
import io.ktor.server.request.host
import io.ktor.server.request.port
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nonEmptyOrNull
import java.io.File

fun main() {
    embeddedServer(Netty, port = Ports.SERVER, host = Hosts.SERVER, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(createApplicationPlugin(name = "CDNPlugin") {
        onCallRespond { call ->
            transformBody {
                call.attributes.getOrNull(AttributeKey.ToCDN).nonEmptyOrNull()?.let { toHost ->
                    call.request.host().let { fromHost ->
                        toHost.takeUnless {
                            fromHost.equals(
                                BuildConfig.CDN_ORIGIN,
                                true
                            ) || BuildConfig.CDN_HOST.any {
                                @Suppress("NestedLambdaShadowedImplicitParameter")
                                it.equals(fromHost, true)
                            }
                        }
                    }
                }?.let {
                    @Suppress("NestedLambdaShadowedImplicitParameter")
                    call.response.headers.append(
                        HttpHeaders.Location,
                        "//$it:${call.request.port()}${call.request.uri}"
                    )
                    HttpStatusCode.TemporaryRedirect
                } ?: it
            }
        }
    })
    routing {
        listOf(
            Paths.FILE,
            Paths.SLIDE_SHOW,
        ).forEach {
            staticFiles("/$it", File("./$it")) {
                modify { _, call ->
                    call.toCDN()
                }
            }
        }
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        var cache: ByteArray? = null
        var contentType: ContentType? = null
        val cacheMutex = Mutex()
        Paths.CAPTURE.also {
            get(it) {
                cacheMutex.withLock {
                    cache?.let { contentType?.let { cache!! to contentType!! } }
                }?.also { (ba, ct) ->
                    call.respondBytes(
                        bytes = ba,
                        contentType = ct,
                        status = HttpStatusCode.OK
                    )
                }
            }
            post(it) {
                call.receive<ByteArray>().also { ba ->
                    call.request.contentType().also { ct ->
                        cacheMutex.withLock {
                            cache = ba
                            contentType = ct
                        }
                        call.respond(HttpStatusCode.NoContent)
                    }
                }
            }
        }
    }
}