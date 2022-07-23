package akio.apps.myrun.data.eapps.impl.model

import com.google.gson.annotations.SerializedName

class StravaStravaTokenRefresh(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,
)
