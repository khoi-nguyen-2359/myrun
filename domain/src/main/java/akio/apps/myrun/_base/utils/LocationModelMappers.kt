package akio.apps.myrun._base.utils

import akio.apps.myrun.data.location.api.model.Location

typealias GmsLatLng = com.google.android.libraries.maps.model.LatLng

fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
