import KKit.shared.BuildConfig
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    js().run {
        hello()
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "${world()}[" +
                        "current:${version()}, latest:${BuildConfig.ZIPLINE_JS_VERSION}" +
                        "]" +
                        "@${Hosts.SERVER}" +
                        ":${Ports.SERVER}",
            ) {
                App()
            }
        }
    }
}