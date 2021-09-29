package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.api.model.Location

typealias GmsLatLng = com.google.android.gms.maps.model.LatLng

fun LatLng.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun GmsLatLng.toLatLng(): LatLng = LatLng(latitude, longitude)
