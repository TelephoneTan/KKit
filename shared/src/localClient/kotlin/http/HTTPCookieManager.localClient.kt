package http

import com.multiplatform.webview.cookie.WebViewCookieManager
import io.ktor.http.Cookie
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

val webviewCookieManager = WebViewCookieManager()

internal actual val httpCookieManager: HTTPCookieManager? = object : HTTPCookieManager {
    private val mutex = Mutex()
    override suspend fun setCookie(url: String, cookie: Cookie) {
        mutex.withLock {
            webviewCookieManager.setCookie(
                url, com.multiplatform.webview.cookie.Cookie(
                    name = cookie.name,
                    value = cookie.value,
                    domain = cookie.domain,
                    path = cookie.path,
                    expiresDate = cookie.expires?.timestamp,
                    isSessionOnly = cookie.maxAge == 0 && cookie.expires == null,
                    isSecure = cookie.secure,
                    isHttpOnly = cookie.httpOnly,
                    maxAge = cookie.maxAge.toLong().takeIf { it != 0L }
                )
            )
        }
    }

    override suspend fun getCookies(url: String): List<Cookie> {
        return mutex.withLock {
            webviewCookieManager.getCookies(url).run {
                map {
                    Cookie(
                        name = it.name,
                        value = it.value,
                        maxAge = it.maxAge?.toInt() ?: 0,
                        expires = it.expiresDate?.let { v -> GMTDate(v) },
                        domain = it.domain,
                        path = it.path,
                        secure = it.isSecure ?: false,
                        httpOnly = it.isHttpOnly ?: false
                    )
                }
            }
        }
    }

    override suspend fun removeAllCookies() {
        mutex.withLock {
            webviewCookieManager.removeAllCookies()
        }
    }

    override suspend fun removeCookies(url: String) {
        mutex.withLock {
            webviewCookieManager.removeCookies(url)
        }
    }
}