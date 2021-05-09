package akio.apps.myrun._base.utils

import akio.apps.myrun.data.activityfile.model.ActivityLocation
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.data.routetracking.model.LatLng
import android.location.Location

typealias GmsLatLng = com.google.android.libraries.maps.model.LatLng

fun Location.toLatLng() = LatLng(latitude, longitude)
fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun TrackingLocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun ActivityLocation.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun LatLng.toGmsLatLng() =
    GmsLatLng(latitude, longitude)
