package akio.apps.myrun.data.place.entity

import com.google.gson.annotations.SerializedName

data class ReverseGeocodingResult(
    @SerializedName("address_components")
    val addressComponents: List<GoogleMapAddressComponent>
)
