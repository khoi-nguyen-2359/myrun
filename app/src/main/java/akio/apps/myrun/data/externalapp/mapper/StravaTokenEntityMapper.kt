package akio.apps.myrun.data.externalapp.mapper

import akio.apps.myrun.data.externalapp.entity.StravaTokenEntity
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.entity.StravaAthlete
import javax.inject.Inject

class StravaTokenEntityMapper @Inject constructor() {
    fun map(input: StravaTokenEntity): ExternalAppToken.StravaToken {
        return ExternalAppToken.StravaToken(
            input.accessToken, input.refreshToken, StravaAthlete(input.athlete.id)
        )
    }
}