package akio.apps.myrun.data.place

import akio.apps.myrun.data.place.model.PlaceAddressComponent

data class PlaceDetails(
    val id: String,
    val name: String,
    val addressComponents: List<PlaceAddressComponent>,
    val latLng: LatLng
)
