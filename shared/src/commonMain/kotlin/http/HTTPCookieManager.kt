package http

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url

interface HTTPCookieManager : CookiesStorage {
    /**
     * Sets a cookie for the given url.
     * @param url The url for which the cookie is to be set.
     * @param cookie The cookie to be set.
     * */
    suspend fun setCookie(
        url: String,
        cookie: Cookie,
    )

    /**
     * Gets all the cookies for the given url.
     * @param url The url for which the cookies are to be retrieved.
     *
     * @return A list of cookies for the given url.
     * */
    suspend fun getCookies(url: String): List<Cookie>

    /**
     * Removes all the cookies.
     * */
    suspend fun removeAllCookies()

    /**
     * Removes all the cookies for the given url.
     * @param url The url for which the cookies are to be removed.
     * */
    suspend fun removeCookies(url: String)

    override suspend fun get(requestUrl: Url) = getCookies(requestUrl.toString())

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) = setCookie(
        requestUrl.toString(),
        cookie
    )

    override fun close() {}
}

internal expect val httpCookieManager: HTTPCookieManager?