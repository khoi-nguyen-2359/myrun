package akio.apps.myrun.data.externalapp.model

import javax.inject.Inject

class StravaTokenRefreshMapper @Inject constructor() {
    fun map(strava: StravaStravaTokenRefresh): StravaTokenRefresh = with(strava) {
        StravaTokenRefresh(accessToken, refreshToken)
    }
}
