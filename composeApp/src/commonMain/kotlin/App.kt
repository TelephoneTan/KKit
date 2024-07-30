import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import http.HTTPMethod
import http.HTTPRequest
import http.httpClient
import io.ktor.client.plugins.websocket.converter
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.deserialize
import io.ktor.websocket.Frame
import kkit.composeapp.generated.resources.Res
import kkit.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import promise.process
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                var response by remember { mutableStateOf("http response") }
                LaunchedEffect(true) {
                    process {
                        HTTPRequest(
                            method = HTTPMethod.GET,
                            url = "https://tencent.com"
                        ).copy().prepare().string()
                    }.await().result.commit {
                        response = it
                    }
                }
                val messageList = remember { mutableStateListOf<Message>() }
                val listState = rememberLazyListState()
                var restored by remember { mutableStateOf(false) }
                LaunchedEffect(true) {
                    dao.messageDAO().getAll(false).map {
                        it.value.fromJSON<Message>()
                    }.commit {
                        messageList.addAll(0, it)
                        listState.requestScrollToItem(0)
                        restored = true
                    }
                }
                if (restored) {
                    LaunchedEffect(true) {
                        httpClient.webSocket(
                            "${URLBases.WEB_SOCKET_SERVER}/${Paths.MESSENGER}"
                        ) {
                            launch {
                                val ts = Clock.System.now().toEpochMilliseconds()
                                var id = 0
                                while (true) {
                                    Clock.System.now().toLocalDateTime(
                                        TimeZone.currentSystemDefault()
                                    ).also {
                                        sendSerialized(
                                            Message(
                                                id = "$ts-${++id}",
                                                user = "turing",
                                                message = "现在是 $it",
                                                time = it,
                                            )
                                        )
                                    }
                                    delay(1.seconds)
                                }
                            }
                            for (frame in incoming) {
                                if (frame is Frame.Text) {
                                    converter!!.deserialize<Message>(frame).also {
                                        dao.messageDAO().set(it.id, it.toJSON())
                                    }.commit {
                                        messageList.add(0, it)
                                        listState.requestScrollToItem(0)
                                    }
                                }
                            }
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    state = listState,
                ) {
                    var type = 0
                    type++.apply {
                        items(messageList, { type to it.id }, { type }) {
                            Text("[${it.id}] ${it.time} ${it.user}: ${it.message}")
                        }
                    }
                    type++.apply {
                        item(type to type, type) {
                            Image(
                                painterResource(Res.drawable.compose_multiplatform),
                                null
                            )
                        }
                    }
                    type++.apply {
                        item(type to type, type) {
                            Text("Compose: $greeting\n$response")
                        }
                    }
                }
            }
        }
    }
}