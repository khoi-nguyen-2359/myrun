package akio.apps.myrun.domain.tracking.locationprocessor

import akio.apps.myrun.data.location.api.LOG_TAG_LOCATION
import akio.apps.myrun.data.location.api.model.Location
import timber.log.Timber

class LocationSpeedFilter(
    private val maxValidSpeed: Double
) : LocationProcessor {
    override fun process(locations: List<Location>): List<Location> =
        locations.filter {
            val isFiltered = it.speed <= maxValidSpeed
            if (!isFiltered) {
                Timber.tag(LOG_TAG_LOCATION)
                    .d("Location removed because of invalid speed=${it.speed}, max=$maxValidSpeed")
            }
            isFiltered
        }

    companion object {
        // m/s speeds
        const val RUNNING_MAX_SPEED = 12.0
        const val CYCLING_MAX_SPEED = 27.0
    }
}
