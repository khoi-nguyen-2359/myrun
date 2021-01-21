package akio.apps.myrun.data.externalapp.model

import akio.apps.myrun.data.externalapp.entity.StravaAthlete
import akio.apps.myrun.data.externalapp.entity.StravaTokenRefreshEntity

sealed class ExternalAppToken {
    class StravaToken(
        val accessToken: String,
        val refreshToken: String,
        val athlete: StravaAthlete
    ) : ExternalAppToken() {

        constructor(refreshToken: StravaTokenRefreshEntity, athlete: StravaAthlete) : this(
            refreshToken.accessToken,
            refreshToken.refreshToken,
            athlete
        )
    }
}
