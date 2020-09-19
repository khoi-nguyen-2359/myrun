package akio.apps.myrun.feature._base

import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun TrackingLocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)

typealias GmsLatLng = LatLng