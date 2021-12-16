package akio.apps.myrun.data.route.api

import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.route.api.model.RouteDetailModel
import akio.apps.myrun.data.route.api.model.RouteModel

interface RouteRepository {
    suspend fun getRoute(routeId: String): RouteModel
    suspend fun getRouteWaypoints(routeId: String): List<LatLng>
    suspend fun getRouteDetail(routeId: String): RouteDetailModel
}
