package akio.apps.myrun.domain.tracking.locationprocessor

import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.location.api.LOG_TAG_LOCATION
import akio.apps.myrun.data.location.api.model.Location
import timber.log.Timber

class LocationSpeedFilter private constructor(
    private val validMaxSpeed: Double,
) : LocationProcessor {
    override fun process(locations: List<Location>): List<Location> =
        locations.filter {
            val isFiltered = it.speed <= validMaxSpeed
            if (!isFiltered) {
                Timber.tag(LOG_TAG_LOCATION)
                    .d("Location removed because of invalid speed=${it.speed}, max=$validMaxSpeed")
            }
            isFiltered
        }

    companion object {
        // m/s speeds
        private const val RUNNING_MAX_SPEED = 12.0
        private const val CYCLING_MAX_SPEED = 27.0

        fun createInstance(activityType: ActivityType): LocationSpeedFilter {
            val maxValidSpeed = when (activityType) {
                ActivityType.Running -> RUNNING_MAX_SPEED
                ActivityType.Cycling -> CYCLING_MAX_SPEED
                else -> Double.MAX_VALUE
            }
            return LocationSpeedFilter(maxValidSpeed)
        }
    }
}
