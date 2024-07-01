import KKit.shared.BuildConfig

object HTTPSchemes {
    const val SERVER = "http"
}

object Hosts {
    const val SERVER = BuildConfig.SERVER_HOST
}

object Ports {
    const val SERVER = BuildConfig.SERVER_PORT
}

object HTTPBases {
    const val SERVER = BuildConfig.SERVER_HTTP_BASE
}

object Paths {
    const val JS_CORE = BuildConfig.JS_CORE_SERVER_DIR
    const val FILE = "file"
}

object URLSuffixes {
    fun jsCore(version: Int) = "/${Paths.JS_CORE}/$version/${BuildConfig.JS_CORE_FILE_NAME}"
    const val JS_CORE_VERSION_LATEST = "/${Paths.JS_CORE}/version/latest"
}