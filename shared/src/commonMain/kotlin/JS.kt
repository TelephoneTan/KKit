interface JS {
    fun hello()
    fun world(): String
    fun version(): Int
}

expect suspend fun js(): JS