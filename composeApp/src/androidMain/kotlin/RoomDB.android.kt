import android.annotation.SuppressLint
import androidx.room.Room
import pub.telephone.kkit.MyApp

@SuppressLint("StaticFieldLeak")
internal actual val dbBuilder = run {
    val appContext = MyApp.context
    Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = appContext.getDatabasePath("my_room.db").absolutePath
    )
}