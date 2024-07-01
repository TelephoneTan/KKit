import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "hello",
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "below title ${
                        LaunchedJS("default") {
                            "${world()}[v](${ms(Money.RMB)})"
                        }
                    }"
                )
                Box(Modifier.weight(1f)) {
                    App()
                }
            }
        }
    }
}