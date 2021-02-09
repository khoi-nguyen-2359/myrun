package akio.apps.myrun.data.externalapp.mapper

import akio.apps.myrun.data.externalapp.entity.FirestoreProvidersEntity
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.StravaAthlete
import javax.inject.Inject

class FirestoreStravaTokenMapper @Inject constructor() {
    fun map(input: FirestoreProvidersEntity.FirestoreStravaToken): ExternalAppToken.StravaToken {
        return ExternalAppToken.StravaToken(
            input.accessToken,
            input.refreshToken,
            StravaAthlete(input.athlete.id)
        )
    }

    fun mapReversed(input: ExternalAppToken.StravaToken): FirestoreProvidersEntity.FirestoreStravaToken {
        return FirestoreProvidersEntity.FirestoreStravaToken(
            input.accessToken,
            input.refreshToken,
            FirestoreProvidersEntity.FirestoreStravaAthlete(input.athlete.id)
        )
    }
}
