package akio.apps.myrun.data.eapps.impl.model

import akio.apps.myrun.data.eapps.api.model.StravaTokenRefresh
import javax.inject.Inject

class StravaTokenRefreshMapper @Inject constructor() {
    fun map(strava: StravaStravaTokenRefresh): StravaTokenRefresh = with(strava) {
        StravaTokenRefresh(accessToken, refreshToken)
    }
}
