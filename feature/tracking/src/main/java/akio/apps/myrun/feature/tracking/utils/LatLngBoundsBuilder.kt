package akio.apps.myrun.feature.tracking.utils

import com.google.android.gms.maps.model.LatLngBounds

private typealias GmsLatLng = com.google.android.gms.maps.model.LatLng

class LatLngBoundsBuilder {
    private val internalBuilder: LatLngBounds.Builder = LatLngBounds.builder()
    private var isEmpty: Boolean = true

    fun include(gmsLatLng: GmsLatLng): LatLngBoundsBuilder {
        internalBuilder.include(gmsLatLng)
        isEmpty = false
        return this
    }

    fun build(): LatLngBounds? = if (isEmpty) {
        null
    } else {
        internalBuilder.build()
    }
}
