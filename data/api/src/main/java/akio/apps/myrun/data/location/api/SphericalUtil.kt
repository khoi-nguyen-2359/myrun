package akio.apps.myrun.data.location.api

import akio.apps.myrun.data.location.api.model.LatLng

interface SphericalUtil {
    fun computeDistanceBetween(p1: LatLng, p2: LatLng): Double
}
