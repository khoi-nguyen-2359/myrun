package akio.apps.myrun.data.place.model

import com.google.gson.annotations.SerializedName

data class ReverseGeocodingResponse(
    @SerializedName("results")
    val results: List<ReverseGeocodingResult>
)
