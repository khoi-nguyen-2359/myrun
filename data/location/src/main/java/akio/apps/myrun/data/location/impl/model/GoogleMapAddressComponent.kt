package akio.apps.myrun.data.location.impl.model

import com.google.gson.annotations.SerializedName

data class GoogleMapAddressComponent(
    @SerializedName("long_name")
    val longName: String,
    @SerializedName("short_name")
    val shortName: String,
    @SerializedName("types")
    val types: List<String>
)
