package akio.apps.myrun.data.eapps.impl.mapper

import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.StravaAthlete
import akio.apps.myrun.data.eapps.impl.model.StravaStravaToken
import javax.inject.Inject

class StravaStravaTokenMapper @Inject constructor() {
    fun map(input: StravaStravaToken): ExternalAppToken.StravaToken {
        return ExternalAppToken.StravaToken(
            input.accessToken, input.refreshToken, StravaAthlete(input.athlete.id)
        )
    }
}
