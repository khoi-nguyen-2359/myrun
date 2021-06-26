package akio.apps.myrun._base.utils

import akio.apps.myrun.data.location.LocationEntity

typealias GmsLatLng = com.google.android.libraries.maps.model.LatLng

fun LocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)
