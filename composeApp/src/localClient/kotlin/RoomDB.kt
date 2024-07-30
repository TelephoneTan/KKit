import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert

@Database(entities = [AppEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDAO(): AppDAO
}

@Entity(primaryKeys = ["table", "key"])
data class AppEntity(
    val table: String,
    val key: String,
    val value: String
)

@Dao
interface AppDAO {
    @Upsert
    suspend fun upsert(item: AppEntity)

    @Query("SELECT * FROM AppEntity where `table`=:table and `key`=:key")
    suspend fun select(table: String, key: String): AppEntity?

    @Query("SELECT * FROM AppEntity where `table`=:table order by `key` asc")
    suspend fun selectAllASC(table: String): List<AppEntity>

    @Query("SELECT * FROM AppEntity where `table`=:table order by `key` desc")
    suspend fun selectAllDESC(table: String): List<AppEntity>

    @Query("delete from AppEntity where `table`=:table and `key`=:key")
    suspend fun delete(table: String, key: String): Int

    @Query("delete from AppEntity where `table`=:table")
    suspend fun clear(table: String): Int
}

internal expect val dbBuilder: RoomDatabase.Builder<AppDatabase>