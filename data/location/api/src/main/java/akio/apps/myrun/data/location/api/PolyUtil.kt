package akio.apps.myrun.data.location.api

import akio.apps.myrun.data.location.api.model.LatLng

interface PolyUtil {
    fun encode(path: List<LatLng>): String
}
