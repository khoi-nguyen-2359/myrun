package akio.apps.myrun.domain.migration.task

import akio.apps.myrun.domain.migration.AppVersion
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * 1.5.0 migration:
 * - Disable speed filter from location processors.
 */
class MigrationTask10500 @Inject constructor(
    private val routeTrackingConfiguration: RouteTrackingConfiguration,
) : MigrationTask(AppVersion.V1_5_0) {
    override suspend fun migrateInternal() {
        val locationProcessConfig = routeTrackingConfiguration.getLocationProcessingConfig().first()
        if (!locationProcessConfig.isSpeedFilterEnabled) {
            return
        }
        val migrationConfig = locationProcessConfig.copy(isSpeedFilterEnabled = false)
        routeTrackingConfiguration.setLocationProcessingConfig(migrationConfig)
    }
}
