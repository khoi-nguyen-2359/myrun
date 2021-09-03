package akio.apps.myrun.domain.migration.task

import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * 1.5.0 migration:
 * - Disable speed filter from location processors.
 */
class MigrationTask10500 @Inject constructor(
    private val routeTrackingConfiguration: RouteTrackingConfiguration,
) : MigrationTask(10500) {
    override suspend fun migrate() = try {
        val locationProcessConfig = routeTrackingConfiguration.getLocationProcessingConfig().first()
        val migrationConfig = locationProcessConfig.copy(isSpeedFilterEnabled = false)
        routeTrackingConfiguration.setLocationProcessingConfig(migrationConfig)
        true
    } catch (ex: Exception) {
        Timber.e(ex)
        false
    }
}
