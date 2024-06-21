package pub.telephone.kkit

import KKit.shared.BuildConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.log
import io.ktor.server.response.respond

suspend fun ApplicationCall.redirect(url: String, code: HttpStatusCode) {
    response.headers.append(HttpHeaders.Location, url)
    respond(code)
}

val ApplicationCall.log get() = application.log

fun ApplicationCall.toCDN() {
    BuildConfig.CDN_HOST.firstOrNull()?.let {
        attributes.put(AttributeKey.ToCDN, it)
    }
}