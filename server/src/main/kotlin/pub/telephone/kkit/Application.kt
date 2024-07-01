package pub.telephone.kkit

import Greeting
import Hosts
import KKit.shared.BuildConfig
import Paths
import Ports
import URLSuffixes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.request.host
import io.ktor.server.request.port
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
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
                            ) || BuildConfig.CDN_HOST.any { it.equals(fromHost, true) }
                        }
                    }
                }?.let {
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
            Paths.JS_CORE,
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
        get(URLSuffixes.JS_CORE_VERSION_LATEST) {
            call.respond(BuildConfig.JS_CORE_VERSION.toString())
        }
    }
}