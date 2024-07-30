import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import promise.task.TaskOnce
import promise.toPromiseScope
import kotlin.coroutines.coroutineContext

class DAOImpl(tableName: String, private val appDAO: AppDAO) : DAO(tableName) {
    override suspend fun get(key: String): String? {
        return appDAO.select(tableName, key)?.value
    }

    override suspend fun getAll(ascending: Boolean): Map<String, String> {
        return (
                if (ascending)
                    appDAO.selectAllASC(tableName)
                else
                    appDAO.selectAllDESC(tableName)
                ).associate { it.key to it.value }
    }

    override suspend fun set(key: String, value: String) {
        return appDAO.upsert(
            AppEntity(
                table = tableName,
                key = key,
                value = value,
            )
        )
    }

    override suspend fun delete(key: String) {
        appDAO.delete(tableName, key)
    }

    override suspend fun clear() {
        appDAO.clear(tableName)
    }
}

actual val dao = object : DAOProvider {
    private val buildDatabase = TaskOnce(Job().toPromiseScope()) {
        promise {
            rsv(
                dbBuilder
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .setDriver(BundledSQLiteDriver())
                    .setQueryCoroutineContext(Dispatchers.IO)
                    .build()
            )
        }
    }

    private val appDAO = TaskOnce(Job().toPromiseScope()) {
        promise {
            rsv(
                DAOImpl(
                    "message",
                    buildDatabase.perform().await().appDAO()
                )
            )
        }
    }

    override suspend fun messageDAO(): DAO {
        return appDAO.perform(coroutineContext.toPromiseScope()).await()
    }
}
