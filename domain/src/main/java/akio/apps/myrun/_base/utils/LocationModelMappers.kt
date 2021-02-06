package akio.apps.myrun._base.utils

import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.feature.routetracking.model.LatLng
import android.location.Location

typealias GmsLatLng = com.google.android.gms.maps.model.LatLng

fun Location.toLatLng() = LatLng(latitude, longitude)
fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun TrackingLocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun LatLng.toGmsLatLng() =
    GmsLatLng(latitude, longitude)

fun LocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)
