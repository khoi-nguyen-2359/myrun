package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.PolyUtil
import akio.apps.myrun.data.location.api.model.LatLng
import javax.inject.Inject

private typealias GmsPolyUtil = com.google.maps.android.PolyUtil

class PolyUtilImpl @Inject constructor() : PolyUtil {
    override fun encode(path: List<LatLng>): String =
        GmsPolyUtil.encode(path.map { it.toGmsLatLng() })

    override fun decode(encPath: String): List<LatLng> =
        GmsPolyUtil.decode(encPath).map { it.toLatLng() }
}
