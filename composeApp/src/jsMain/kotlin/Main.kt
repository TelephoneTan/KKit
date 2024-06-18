import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

fun main() {
    console.log(Greeting().greet())
    if (isBrowser) {
        @OptIn(ExperimentalComposeUiApi::class)
        ComposeViewport(document.body!!) {
            App()
        }
    }
}