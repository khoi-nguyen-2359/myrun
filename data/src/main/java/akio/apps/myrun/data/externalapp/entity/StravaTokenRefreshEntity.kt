package akio.apps.myrun.data.externalapp.entity

import com.google.gson.annotations.SerializedName

class StravaTokenRefreshEntity(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)
