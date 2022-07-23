package akio.apps.myrun.data.location.api.model

data class PlaceDetails(
    val id: String,
    val name: String,
    val addressComponents: List<PlaceAddressComponent>,
    val latLng: LatLng,
)
