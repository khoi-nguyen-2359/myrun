package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.PolyUtil
import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.di.LocationDataScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

typealias GmsPolyUtil = com.google.maps.android.PolyUtil

@ContributesBinding(LocationDataScope::class)
class PolyUtilImpl @Inject constructor() : PolyUtil {
    override fun encode(path: List<LatLng>): String =
        GmsPolyUtil.encode(path.map { it.toGmsLatLng() })

    override fun decode(encPath: String): List<LatLng> =
        GmsPolyUtil.decode(encPath).map { it.toLatLng() }
}
