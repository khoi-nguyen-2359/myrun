package akio.apps.myrun.data.location.impl.mapbox

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.location.api.DirectionDataSource
import akio.apps.myrun.data.location.api.WaypointReducer
import akio.apps.myrun.data.location.api.model.LatLng
import androidx.annotation.WorkerThread
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.matching.v5.MapboxMapMatching
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class MapBoxDirectionDataSource @Inject constructor(
    private val mapBoxAccessToken: MapBoxAccessToken,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : DirectionDataSource {

    private val waypointReducer: WaypointReducer = WaypointReducer()

    @WorkerThread
    override suspend fun getWalkingDirections(
        waypoints: List<LatLng>,
    ): List<LatLng> = withContext(ioDispatcher) {
        val mapMatchingRequest = MapboxMapMatching.builder()
            .accessToken(mapBoxAccessToken.value)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .steps(false)
            .tidy(true)
            .overview(DirectionsCriteria.OVERVIEW_SIMPLIFIED)
            .geometries(DirectionsCriteria.GEOMETRY_POLYLINE)
            .coordinates(
                waypointReducer.reduce(waypoints, MAX_MAP_MATCHING_API_WAYPOINT)
                    .map { Point.fromLngLat(it.longitude, it.latitude) }
            )
            .build()

        mapMatchingRequest.enableDebug(true)
        val responseBody = mapMatchingRequest.executeCall().body()
        val geometry = responseBody?.matchings()?.firstOrNull()?.geometry()
        val gmsWaypoints = geometry?.let {
            PolylineUtils.decode(geometry, GOOGLE_POLYLINE_PRECISION)
        } ?: emptyList()
        gmsWaypoints.map { LatLng(it.latitude(), it.longitude()) }
    }

    companion object {
        // limit of mapbox map matching API
        private const val MAX_MAP_MATCHING_API_WAYPOINT = 100

        private const val GOOGLE_POLYLINE_PRECISION = 5
    }
}
