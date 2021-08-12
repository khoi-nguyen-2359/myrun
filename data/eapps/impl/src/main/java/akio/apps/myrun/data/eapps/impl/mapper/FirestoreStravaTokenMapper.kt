package akio.apps.myrun.data.eapps.impl.mapper

import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.StravaAthlete
import akio.apps.myrun.data.eapps.impl.model.FirestoreProviders
import javax.inject.Inject

class FirestoreStravaTokenMapper @Inject constructor() {
    fun map(input: FirestoreProviders.FirestoreStravaToken): ExternalAppToken.StravaToken {
        return ExternalAppToken.StravaToken(
            input.accessToken,
            input.refreshToken,
            StravaAthlete(input.athlete.id)
        )
    }

    fun mapReversed(
        input: ExternalAppToken.StravaToken,
    ): FirestoreProviders.FirestoreStravaToken {
        return FirestoreProviders.FirestoreStravaToken(
            input.accessToken,
            input.refreshToken,
            FirestoreProviders.FirestoreStravaAthlete(input.athlete.id)
        )
    }
}
