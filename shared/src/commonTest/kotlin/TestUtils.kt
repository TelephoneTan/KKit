import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val time get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

private val logMutex = Mutex()

fun log(msg: Any?) {
    logMutex.synchronizedAsync {
        println("【${time}】$msg")
    }
}

private const val blockStart = "<<<<<<<<<<<<<"
private const val blockEnd = ">>>>>>>>>>>>>"

private fun logStart(msg: Any?) = println("【${time}】$msg $blockStart")
private fun logEnd(msg: Any?) = println("【${time}】$blockEnd $msg")
fun logBlock(msg: Any?, block: () -> Unit) {
    logMutex.synchronizedAsync {
        logStart(msg)
        block()
        logEnd(msg)
    }
}