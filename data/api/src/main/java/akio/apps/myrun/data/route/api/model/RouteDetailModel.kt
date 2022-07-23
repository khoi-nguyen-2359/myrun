package akio.apps.myrun.data.route.api.model

import akio.apps.myrun.data.location.api.model.LatLng

data class RouteDetailModel(
    val routeModel: RouteModel,
    val waypoints: List<LatLng>,
)
