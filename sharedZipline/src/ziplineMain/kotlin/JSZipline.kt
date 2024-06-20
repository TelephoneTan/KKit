import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineService

interface JSZipline : JS, ZiplineService {
    companion object {
        const val NAME = "JSZipline"
        var Zipline.jsZipline: JSZipline
            get() = take(NAME)
            set(value) = bind(NAME, value)
    }
}