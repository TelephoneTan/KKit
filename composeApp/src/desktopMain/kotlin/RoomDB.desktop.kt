import androidx.room.Room
import java.io.File

internal actual val dbBuilder = run {
    Room.databaseBuilder<AppDatabase>(
        name = File(System.getProperty("java.io.tmpdir"), "my_room.db").absolutePath,
    )
}