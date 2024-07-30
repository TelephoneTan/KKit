abstract class DAO(val tableName: String) {
    abstract suspend fun get(key: String): String?
    abstract suspend fun getAll(ascending: Boolean = true): Map<String, String>
    abstract suspend fun set(key: String, value: String)
    abstract suspend fun delete(key: String)
    abstract suspend fun clear()
}

interface DAOProvider {
    suspend fun messageDAO(): DAO
}

expect val dao: DAOProvider