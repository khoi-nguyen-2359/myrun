package akio.apps.myrun.domain.activity

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.location.api.model.LatLng

fun ActivityLocation.getLatLng() = LatLng(latitude, longitude)
