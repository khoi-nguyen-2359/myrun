package akio.apps.myrun.feature.base.map

import akio.apps.myrun.data.route.api.model.RouteDetailModel
import com.google.android.gms.maps.model.LatLngBounds

fun RouteDetailModel.getLatLngBounds(): LatLngBounds? {
    val builder = LatLngBoundsBuilder()
    waypoints.forEach { builder.include(it.toGmsLatLng()) }
    return builder.build()
}
