import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val user: String,
    val message: String,
    val time: LocalDateTime,
)