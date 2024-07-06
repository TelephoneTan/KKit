package log

import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import synchronizedAsync

val time get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

private val logMutex = Mutex()

private fun logPrintln(msg: Any?) {
    println("【${time}】$msg")
}

fun log(msg: Any?) {
    logMutex.synchronizedAsync {
        logPrintln(msg)
    }
}

private const val blockStart = "<<<<<<<<<<<<<"
private const val blockEnd = ">>>>>>>>>>>>>"

private fun logStart(msg: Any?) = logPrintln("$msg $blockStart")
private fun logEnd(msg: Any?) = logPrintln("$blockEnd $msg")
fun logBlock(msg: Any?, block: () -> Unit) {
    logMutex.synchronizedAsync {
        logStart(msg)
        block()
        logEnd(msg)
    }
}