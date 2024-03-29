package akio.apps.myrun.domain.migration.task

import akio.apps.myrun.domain.migration.AppVersion
import timber.log.Timber

abstract class MigrationTask(val version: AppVersion) {
    protected abstract suspend fun migrateInternal()

    suspend fun migrate(): Boolean = try {
        migrateInternal()
        true
    } catch (ex: Exception) {
        Timber.e(ex)
        false
    }
}
