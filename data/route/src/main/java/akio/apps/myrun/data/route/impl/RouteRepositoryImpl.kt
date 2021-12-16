package akio.apps.myrun.data.route.impl

import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.route.api.RouteRepository
import akio.apps.myrun.data.route.api.model.RouteDetailModel
import akio.apps.myrun.data.route.api.model.RouteModel
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor() : RouteRepository {
    override suspend fun getRoute(routeId: String): RouteModel {
        TODO("Not yet implemented")
    }

    override suspend fun getRouteWaypoints(routeId: String): List<LatLng> {
        TODO("Not yet implemented")
    }

    override suspend fun getRouteDetail(routeId: String): RouteDetailModel {
        TODO("Not yet implemented")
    }
}
