package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.DirectionDataSource
import akio.apps.myrun.data.location.api.PolyUtil
import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.impl.model.GoogleMapDirectionApiKey
import akio.apps.myrun.data.location.impl.model.MapApiStatus
import akio.apps.myrun.wiring.common.NamedIoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

class GoogleDirectionDataSource @Inject constructor(
    private val googleMapDirectionApi: GoogleMapDirectionApi,
    private val googleMapDirectionApiKey: GoogleMapDirectionApiKey,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val polyUtil: PolyUtil
) : DirectionDataSource {

    override suspend fun getWalkingDirections(
        waypoints: List<LatLng>,
    ): List<LatLng> = withContext(ioDispatcher) {
        if (waypoints.size < 2)
            return@withContext waypoints

        val origin = waypoints.first()
        val destination = waypoints.last()
        val originQuery = "${origin.latitude},${origin.longitude}"
        val destinationOrigin = "${destination.latitude},${destination.longitude}"
        val simplified = simplifyWaypoints(waypoints)
        val encodedWaypoint = polyUtil.encode(simplified)
        val encodedWaypointParam = "enc:$encodedWaypoint:"
        val response = googleMapDirectionApi.getWalkingDirections(
            originQuery,
            destinationOrigin,
            encodedWaypointParam,
            googleMapDirectionApiKey.value
        )

        if (response.status != MapApiStatus.OK) {
            throw Exception(response.status.name)
        }

        val defaultRoute = response.routes.first()

        polyUtil.decode(defaultRoute.overviewPolyline.points)
    }

    private fun simplifyWaypoints(drawnWaypoints: List<LatLng>): List<LatLng> {
        Timber.d("original waypoints size = ${drawnWaypoints.size}")
        if (drawnWaypoints.size <= MAX_API_WAYPOINT) {
            return drawnWaypoints
        }

        val waypointIndexStep = drawnWaypoints.size / (MAX_API_WAYPOINT - 1f)
        Timber.d("step $waypointIndexStep")
        var nextWaypointIndex = waypointIndexStep

        val simplifiedWaypoints = mutableListOf<LatLng>()
        for (index in waypointIndexStep.toInt() until drawnWaypoints.size - 1) {
            if (index == nextWaypointIndex.toInt()) {
                Timber.d("index $index")
                nextWaypointIndex += waypointIndexStep
                simplifiedWaypoints.add(drawnWaypoints[index])
            }
        }

        Timber.d("simplified waypoints size = ${simplifiedWaypoints.size}")
        return simplifiedWaypoints
    }

    companion object {
        // limit of google direction api
        const val MAX_API_WAYPOINT = 10
    }
}
