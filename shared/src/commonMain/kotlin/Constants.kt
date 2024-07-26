import KKit.shared.BuildConfig

@Suppress("unused")
object HTTPSchemes {
    const val SERVER = "http"
}

object Hosts {
    const val SERVER = BuildConfig.SERVER_HOST
}

object Ports {
    const val SERVER = BuildConfig.SERVER_PORT
}

@Suppress("unused")
object HTTPBases {
    const val SERVER = BuildConfig.SERVER_HTTP_BASE
}

object Paths {
    const val FILE = "file"
    const val SLIDE_SHOW = "slide-show"
    const val CAPTURE = "capture"
}

@Suppress("unused", "RemoveEmptyClassBody")
object URLSuffixes {
}