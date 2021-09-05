package akio.apps.myrun.domain.migration.task

abstract class MigrationTask(private val migrateVersionCode: Int) {
    fun isApplicable(currVersionCode: Int) = currVersionCode >= migrateVersionCode
    abstract suspend fun migrate(): Boolean
}
