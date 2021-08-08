package akio.apps.myrun.data.externalapp.model

import com.google.gson.annotations.SerializedName

data class StravaStravaToken(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("athlete")
    val athlete: StravaAthlete
) {
    data class StravaAthlete(
        @SerializedName("id")
        val id: Long
    )
}
