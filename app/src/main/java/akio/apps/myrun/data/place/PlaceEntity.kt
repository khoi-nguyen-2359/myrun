package akio.apps.myrun.data.place

import akio.apps.myrun.data.location.LatLngEntity

data class PlaceEntity(
    val id: String,
    val name: String,
    val addressComponents: List<PlaceAddressComponent>,
    val latLng: LatLngEntity
)
