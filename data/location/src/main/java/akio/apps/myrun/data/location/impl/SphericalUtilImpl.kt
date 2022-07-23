package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.SphericalUtil
import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.di.LocationDataScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

private typealias GmsSphericalUtil = com.google.maps.android.SphericalUtil

@ContributesBinding(LocationDataScope::class)
class SphericalUtilImpl @Inject constructor() : SphericalUtil {
    override fun computeDistanceBetween(p1: LatLng, p2: LatLng): Double =
        GmsSphericalUtil.computeDistanceBetween(p1.toGmsLatLng(), p2.toGmsLatLng())
}
