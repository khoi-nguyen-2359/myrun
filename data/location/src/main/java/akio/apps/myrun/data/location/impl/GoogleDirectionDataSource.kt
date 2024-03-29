package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.DirectionDataSource
import akio.apps.myrun.data.location.api.PolyUtil
import akio.apps.myrun.data.location.api.WaypointReducer
import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.impl.model.GoogleMapDirectionApiKey
import akio.apps.myrun.data.location.impl.model.MapApiStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDirectionDataSource @Inject constructor(
    private val googleMapDirectionApi: GoogleMapDirectionApi,
    private val googleMapDirectionApiKey: GoogleMapDirectionApiKey,
    private val polyUtil: PolyUtil,
) : DirectionDataSource {

    private val wayPointReducer: WaypointReducer = WaypointReducer()

    override suspend fun getWalkingDirections(
        waypoints: List<LatLng>,
    ): List<LatLng> {
        if (waypoints.size < 2) {
            return waypoints
        }

        val origin = waypoints.first()
        val destination = waypoints.last()
        val originQuery = "${origin.latitude},${origin.longitude}"
        val destinationOrigin = "${destination.latitude},${destination.longitude}"
        val simplified = wayPointReducer.reduce(waypoints, MAX_API_WAYPOINT)
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

        return polyUtil.decode(defaultRoute.overviewPolyline.points)
    }

    companion object {
        // limit of google direction api
        const val MAX_API_WAYPOINT = 10
    }
}
