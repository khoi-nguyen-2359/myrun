package akio.apps.myrun.feature._base

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun Location.toGoogleLatLng() = com.google.android.gms.maps.model.LatLng(latitude, longitude)

typealias GmsLatLng = LatLng