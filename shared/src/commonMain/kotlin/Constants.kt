import KKit.shared.BuildConfig

@Suppress("unused")
object Schemes {
    const val SERVER = BuildConfig.SERVER_SCHEME
    const val WEB_SOCKET_SERVER = BuildConfig.WEB_SOCKET_SERVER_SCHEME
}

object Hosts {
    const val SERVER = BuildConfig.SERVER_HOST
    const val WEB_SOCKET_SERVER = BuildConfig.WEB_SOCKET_SERVER_HOST
}

object Ports {
    const val SERVER = BuildConfig.SERVER_PORT
    const val WEB_SOCKET_SERVER = BuildConfig.WEB_SOCKET_SERVER_PORT
}

@Suppress("unused")
object URLBases {
    const val SERVER =
        "${Schemes.SERVER}://${Hosts.SERVER}:${Ports.SERVER}"
    const val WEB_SOCKET_SERVER =
        "${Schemes.WEB_SOCKET_SERVER}://${Hosts.WEB_SOCKET_SERVER}:${Ports.WEB_SOCKET_SERVER}"
}

object Paths {
    const val FILE = "file"
    const val SLIDE_SHOW = "slide-show"
    const val CAPTURE = "capture"
    const val MESSENGER = "messenger"
}

const val WEBSOCKET_PING_INTERVAL_SECONDS = 30