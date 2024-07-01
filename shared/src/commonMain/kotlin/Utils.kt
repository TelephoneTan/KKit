import kotlinx.serialization.json.Json

fun String?.nonEmptyOrNull() = this?.takeIf { it.isNotEmpty() }

val json = Json {
    encodeDefaults = true
    explicitNulls = true
    ignoreUnknownKeys = true
}