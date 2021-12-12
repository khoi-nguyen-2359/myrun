package akio.apps.myrun.domain.activity.api

import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.domain.activity.api.model.ActivityLocation

fun ActivityLocation.getLatLng() = LatLng(latitude, longitude)
