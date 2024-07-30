import androidx.room.Room
import platform.Foundation.NSHomeDirectory

internal actual val dbBuilder = run {
    Room.databaseBuilder<AppDatabase>(
        name = NSHomeDirectory() + "/my_room.db",
        factory = { AppDatabase::class.instantiateImpl() }
    )
}