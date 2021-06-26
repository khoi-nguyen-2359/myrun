package akio.apps.myrun._base.utils

import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.TrackingLocationEntity

typealias GmsLatLng = com.google.android.libraries.maps.model.LatLng

fun TrackingLocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun LocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)
