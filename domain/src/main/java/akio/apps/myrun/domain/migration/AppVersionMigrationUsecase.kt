package akio.apps.myrun.domain.migration

import akio.apps.myrun.data.user.api.AppVersionMigrationState
import akio.apps.myrun.domain.migration.task.MigrationTask10500
import dagger.Lazy
import javax.inject.Inject

class AppVersionMigrationUsecase @Inject constructor(
    private val appVersionMigrationState: AppVersionMigrationState,
    private val lazyMigrationTask10500: Lazy<MigrationTask10500>
) {
    suspend fun migrate(currVersionCode: Int) {
        if (appVersionMigrationState.isMigrationCompleted()) {
            return
        }
        listOf(
            lazyMigrationTask10500.get()
        )
            .filter { task -> task.isApplicable(currVersionCode) }
            .map { task -> task.migrate() }
            .indexOf(false)
            .takeIf { it == -1 }
            ?.let { appVersionMigrationState.setMigrationCompleted(true) }
    }
}
