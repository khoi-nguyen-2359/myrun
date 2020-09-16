package akio.apps.myrun.feature._base

import android.location.Location

fun Location.toGoogleLatLng() = com.google.android.gms.maps.model.LatLng(latitude, longitude)