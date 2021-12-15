package akio.apps.myrun.data.location.impl.model

import com.google.gson.annotations.SerializedName

data class ReverseGeocodingResult(
    @SerializedName("address_components")
    val addressComponents: List<GoogleMapAddressComponent>
)
