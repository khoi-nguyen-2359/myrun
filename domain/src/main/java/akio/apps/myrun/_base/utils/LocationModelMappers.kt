package akio.apps.myrun._base.utils

import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.data.routetracking.model.LatLng
import android.location.Location

typealias GmsLatLng = com.google.android.libraries.maps.model.LatLng

fun Location.toLatLng() = LatLng(latitude, longitude)
fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun TrackingLocationEntity.trackingLocationToGmsLatLng() = GmsLatLng(latitude, longitude)
fun ActivityLocation.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun LatLng.latLngToGmsLatLng() =
    GmsLatLng(latitude, longitude)
fun Location.toLocationEntity(): LocationEntity = LocationEntity(latitude, longitude, altitude)
