package akio.apps.myrun.data.place.entity

import com.google.gson.annotations.SerializedName

data class ReverseGeocodingResponse(
    @SerializedName("results")
    val results: List<ReverseGeocodingResult>
)
