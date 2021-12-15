package akio.apps.myrun.data.location.api

import akio.apps.myrun.data.location.api.model.LatLng

interface DirectionDataSource {
    suspend fun getWalkingDirections(waypoints: List<LatLng>): List<LatLng>
}
