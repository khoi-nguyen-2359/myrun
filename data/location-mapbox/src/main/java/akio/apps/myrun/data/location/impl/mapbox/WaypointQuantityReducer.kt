package akio.apps.myrun.data.location.impl.mapbox

import akio.apps.myrun.data.location.api.model.LatLng
import javax.inject.Inject
import timber.log.Timber

class WaypointQuantityReducer @Inject constructor() {
    fun reduce(originalWaypoints: List<LatLng>, max: Int): List<LatLng> {
        Timber.d("original waypoints size = ${originalWaypoints.size}")
        if (originalWaypoints.size <= max) {
            return originalWaypoints
        }

        val waypointIndexStep = (originalWaypoints.size - 1) / (max - 1f)
        var nextWaypointIndex = 0.0
        val reducedWaypoints = mutableListOf<LatLng>()
        while (nextWaypointIndex < originalWaypoints.size) {
            reducedWaypoints.add(originalWaypoints[nextWaypointIndex.toInt()])
            nextWaypointIndex += waypointIndexStep
        }

        Timber.d("reduced waypoints size = ${reducedWaypoints.size}")
        return reducedWaypoints
    }
}
