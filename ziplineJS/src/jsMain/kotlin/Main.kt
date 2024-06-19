import JSZipline.Companion.jsZipline
import app.cash.zipline.Zipline

@OptIn(ExperimentalJsExport::class)
@JsExport
fun main() {
    Zipline.get().jsZipline = JSZiplineImpl()
}