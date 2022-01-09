package akio.apps.myrun.feature.base.map

import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.api.model.Location

typealias GmsLatLng = com.google.android.gms.maps.model.LatLng

fun LatLng.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun Location.toLatLng() = LatLng(latitude, longitude)
fun GmsLatLng.toLatLng(): LatLng = LatLng(latitude, longitude)
