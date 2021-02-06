package akio.apps.myrun.data.externalapp.model

sealed class ExternalAppToken {
    class StravaToken(
        val accessToken: String,
        val refreshToken: String,
        val athlete: StravaAthlete
    ) : ExternalAppToken() {

        constructor(refreshToken: StravaTokenRefresh, athlete: StravaAthlete) : this(
            refreshToken.accessToken,
            refreshToken.refreshToken,
            athlete
        )
    }
}
