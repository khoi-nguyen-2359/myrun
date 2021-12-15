package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.model.LatLng

internal typealias GmsLatLng = com.google.android.gms.maps.model.LatLng

internal fun LatLng.toGmsLatLng() = GmsLatLng(latitude, longitude)
internal fun GmsLatLng.toLatLng(): LatLng = LatLng(latitude, longitude)
