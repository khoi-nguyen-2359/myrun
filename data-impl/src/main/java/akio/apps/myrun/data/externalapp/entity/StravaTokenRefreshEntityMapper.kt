package akio.apps.myrun.data.externalapp.entity

import akio.apps.myrun.data.externalapp.model.StravaTokenRefresh
import javax.inject.Inject

class StravaTokenRefreshEntityMapper @Inject constructor() {
    fun map(entity: StravaTokenRefreshEntity): StravaTokenRefresh = with(entity) {
        StravaTokenRefresh(accessToken, refreshToken)
    }
}
