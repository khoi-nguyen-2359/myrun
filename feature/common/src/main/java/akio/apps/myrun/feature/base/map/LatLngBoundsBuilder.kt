package akio.apps.myrun.feature.base.map

import akio.apps.myrun.data.location.api.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class LatLngBoundsBuilder {
    private val internalBuilder: LatLngBounds.Builder = LatLngBounds.builder()
    private var isEmpty: Boolean = true

    fun include(gmsLatLng: GmsLatLng): LatLngBoundsBuilder {
        internalBuilder.include(gmsLatLng)
        isEmpty = false
        return this
    }

    fun include(latLngs: List<LatLng>): LatLngBoundsBuilder {
        latLngs.forEach {
            include(it.toGmsLatLng())
        }
        return this
    }

    fun build(): LatLngBounds? = if (isEmpty) {
        null
    } else {
        internalBuilder.build()
    }
}
