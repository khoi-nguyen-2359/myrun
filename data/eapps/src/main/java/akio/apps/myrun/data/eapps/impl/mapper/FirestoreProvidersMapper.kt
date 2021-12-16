package akio.apps.myrun.data.eapps.impl.mapper

import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.eapps.api.model.ProviderToken
import akio.apps.myrun.data.eapps.api.model.RunningApp
import javax.inject.Inject

class FirestoreProvidersMapper @Inject constructor(
    private val stravaTokenMapper: FirestoreStravaTokenMapper
) {
    fun map(input: akio.apps.myrun.data.eapps.impl.model.FirestoreProviders): ExternalProviders {
        return ExternalProviders(
            strava = input.strava?.token?.let { stravaToken ->
                ProviderToken(RunningApp.Strava, stravaTokenMapper.map(stravaToken))
            }
        )
    }
}
