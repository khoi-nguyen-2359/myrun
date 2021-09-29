package akio.apps.myrun.feature.routetracking.impl

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class LatLngBoundsBuilder() {
    private val internalBuilder: LatLngBounds.Builder = LatLngBounds.builder()
    private var isEmpty: Boolean = true

    fun include(latLng: LatLng) {
        internalBuilder.include(latLng)
        isEmpty = false
    }

    fun build(): LatLngBounds? = if (isEmpty) {
        null
    } else {
        internalBuilder.build()
    }
}
