package akio.apps.myrun._base.utils

import com.google.android.gms.maps.model.LatLngBounds

class LatLngBoundsBuilder() {
    private val internalBuilder: LatLngBounds.Builder = LatLngBounds.builder()
    private var isEmpty: Boolean = true

    fun include(latLng: GmsLatLng) {
        internalBuilder.include(latLng)
        isEmpty = false
    }

    fun build(): LatLngBounds? = if (isEmpty) {
        null
    } else {
        internalBuilder.build()
    }
}
