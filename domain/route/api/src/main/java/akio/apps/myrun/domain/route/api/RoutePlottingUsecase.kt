package akio.apps.myrun.domain.route.api

import akio.apps.myrun.data.location.api.model.LatLng

interface RoutePlottingUsecase {
    suspend fun plotRoute(addingWaypoints: List<LatLng>, currentRoute: List<LatLng>): List<LatLng>
}
