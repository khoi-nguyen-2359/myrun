package akio.apps.myrun.data.externalapp.entity

import com.google.gson.annotations.SerializedName

data class StravaTokenEntity(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("athlete")
    val athlete: StravaAthleteEntity
) {
    data class StravaAthleteEntity(
        @SerializedName("id")
        val id: Long
    )
}

