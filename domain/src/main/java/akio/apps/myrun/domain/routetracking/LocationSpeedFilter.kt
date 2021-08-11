package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.api.model.Location

class LocationSpeedFilter(
    private val maxValidSpeed: Double
) : LocationProcessor {
    override fun process(locations: List<Location>): List<Location> =
        locations.filter { it.speed <= maxValidSpeed }

    companion object {
        // m/s speeds
        const val RUNNING_MAX_SPEED = 12.0
        const val CYCLING_MAX_SPEED = 27.0
    }
}
