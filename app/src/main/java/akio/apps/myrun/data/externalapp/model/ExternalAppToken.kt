package akio.apps.myrun.data.externalapp.model

import akio.apps.myrun.data.externalapp.entity.StravaTokenRefreshEntity
import akio.apps.myrun.data.externalapp.entity.StravaAthlete
import com.google.gson.annotations.SerializedName

sealed class ExternalAppToken {
    class StravaToken(
        @SerializedName("access_token")
        val accessToken: String,

        @SerializedName("refresh_token")
        val refreshToken: String,

        @SerializedName("athlete")
        val athlete: StravaAthlete
    ) : ExternalAppToken() {

        constructor(refreshToken: StravaTokenRefreshEntity, athlete: StravaAthlete) : this(
            refreshToken.accessToken,
            refreshToken.refreshToken,
            athlete
        )
    }
}
