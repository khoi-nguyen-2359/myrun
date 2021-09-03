package akio.apps.myrun.data.user.api

interface AppVersionMigrationState {
    suspend fun isMigrationCompleted(): Boolean
    suspend fun setMigrationCompleted(isCompleted: Boolean)
}
