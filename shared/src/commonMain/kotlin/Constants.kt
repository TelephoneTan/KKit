import KKit.shared.BuildConfig

object HTTPSchemes {
    const val SERVER = "http"
}

object Hosts {
    const val SERVER = "localhost"
}

object Ports {
    const val SERVER = BuildConfig.SERVER_PORT
}

object HTTPBases {
    const val SERVER = BuildConfig.SERVER_HTTP_BASE
}

object Paths {
    const val ZIPLINE_JS = "zipline-js"
    const val FILE = "file"
}

object URLSuffixes {
    fun ziplineJS(version: Int) = "/${Paths.ZIPLINE_JS}/$version/manifest.zipline.json"
    const val ZIPLINE_JS_VERSION_LATEST = "/${Paths.ZIPLINE_JS}/version/latest"
}