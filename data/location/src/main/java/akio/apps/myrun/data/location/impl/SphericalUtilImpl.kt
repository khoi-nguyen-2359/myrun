package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.SphericalUtil
import akio.apps.myrun.data.location.api.model.LatLng
import javax.inject.Inject

private typealias GmsSphericalUtil = com.google.maps.android.SphericalUtil

class SphericalUtilImpl @Inject constructor() : SphericalUtil {
    override fun computeDistanceBetween(p1: LatLng, p2: LatLng): Double =
        GmsSphericalUtil.computeDistanceBetween(p1.toGmsLatLng(), p2.toGmsLatLng())
}
