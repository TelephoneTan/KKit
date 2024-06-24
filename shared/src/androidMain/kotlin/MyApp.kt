import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicReference

class MyApp : Application() {
    private val uiHandler: Handler = Handler(Looper.getMainLooper())

    companion object {
        private val appR = AtomicReference<MyApp>()
        private val app: MyApp get() = appR.get()
        private val ui get() = app.uiHandler
        val context: Context get() = app.applicationContext
        val resources: Resources get() = app.resources
    }
}