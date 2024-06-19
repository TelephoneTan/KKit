import KKit.shared.BuildConfig

const val SERVER_PORT = 8080

object HTTPSchemes {
    const val SERVER = "http"
}

object Hosts {
    const val SERVER = "localhost"
}

object Ports {
    const val SERVER = BuildConfig.ZIPLINE_JS_PORT
}

object HTTPBases {
    const val SERVER = "${HTTPSchemes.SERVER}://${Hosts.SERVER}:${Ports.SERVER}"
}

object URLSuffixes {
    const val ZIPLINE_JS = "/manifest.zipline.json"
}