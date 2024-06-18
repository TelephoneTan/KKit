import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    console.log(Greeting().greet())
    if (isBrowser) {
        onWasmReady {
            @OptIn(ExperimentalComposeUiApi::class)
            ComposeViewport(document.body!!) {
                App()
            }
        }
    }
}