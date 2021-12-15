package akio.apps.myrun.data.location.impl.model

import com.google.gson.annotations.SerializedName

data class ReverseGeocodingResponse(
    @SerializedName("results")
    val results: List<ReverseGeocodingResult>
)
