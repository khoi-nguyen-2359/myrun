package akio.apps.myrun._base.utils

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.api.model.Location

typealias GmsLatLng = com.google.android.gms.maps.model.LatLng

fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun ActivityLocation.toGmsLatLng() = GmsLatLng(latitude, longitude)
