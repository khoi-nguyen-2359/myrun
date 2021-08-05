package akio.apps.myrun.data.externalapp.mapper

import akio.apps.myrun.data.externalapp.model.StravaStravaToken
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.StravaAthlete
import javax.inject.Inject

class StravaStravaTokenMapper @Inject constructor() {
    fun map(input: StravaStravaToken): ExternalAppToken.StravaToken {
        return ExternalAppToken.StravaToken(
            input.accessToken, input.refreshToken, StravaAthlete(input.athlete.id)
        )
    }
}
