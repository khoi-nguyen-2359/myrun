package akio.apps.myrun._base.utils

import com.google.android.libraries.maps.model.LatLngBounds

class LatLngBoundsBuilder() {
    private val internalBuilder: LatLngBounds.Builder = LatLngBounds.builder()
    var isEmpty: Boolean = true
        private set

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
