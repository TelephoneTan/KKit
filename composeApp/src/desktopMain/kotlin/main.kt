import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    js().run {
        hello()
        val latestZ = fetchLatestZiplineVersion()
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "${world()}[" +
                        "current:${version()}, latest:$latestZ" +
                        "]" +
                        "@${Hosts.SERVER}" +
                        ":${Ports.SERVER}",
            ) {
                App()
            }
        }
    }
}