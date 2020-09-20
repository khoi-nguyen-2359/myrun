package akio.apps.myrun.feature._base.utils

import akio.apps.myrun.data.routetracking.model.TrackingLocationEntity
import android.location.Location
import com.google.android.gms.maps.model.LatLng

typealias GmsLatLng = LatLng

fun Location.toLatLng() = akio.apps.myrun.data.location.model.LatLng(latitude, longitude)
fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun TrackingLocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun akio.apps.myrun.data.location.model.LatLng.toGmsLatLng() = GmsLatLng(latitude, longitude)