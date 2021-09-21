package akio.apps.myrun.domain.migration

import akio.apps.myrun.data.user.api.AppMigrationState
import akio.apps.myrun.domain.migration.task.MigrationTask10500
import akio.apps.myrun.domain.version.AppVersion
import javax.inject.Inject

class AppMigrationUsecase @Inject constructor(
    private val appMigrationState: AppMigrationState,
    private val migrationTask10500: MigrationTask10500,
) {
    suspend fun migrate(currAppVersion: AppVersion): Boolean {
        if (appMigrationState.isMigrationSucceeded(currAppVersion.appVersionString)) {
            return true
        }
        var isMigrationSucceeded = true
        listOf(migrationTask10500).forEach { task ->
            var isTaskSucceeded = appMigrationState.isMigrationSucceeded(
                task.version.appVersionString,
                isSingleTask = true
            )
            if (!isTaskSucceeded) {
                isTaskSucceeded = task.migrate()
                appMigrationState.setMigrationSucceeded(
                    task.version.appVersionString,
                    isTaskSucceeded
                )
            }
            isMigrationSucceeded = isTaskSucceeded && isMigrationSucceeded
        }

        appMigrationState.setMigrationSucceeded(
            currAppVersion.appVersionString,
            isMigrationSucceeded
        )

        return isMigrationSucceeded
    }
}
