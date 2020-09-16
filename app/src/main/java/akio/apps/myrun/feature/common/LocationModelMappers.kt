package akio.apps.myrun.feature.common

import android.location.Location

fun Location.toGoogleLatLng() = com.google.android.gms.maps.model.LatLng(latitude, longitude)