package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaTokenRepository
import javax.inject.Inject

class DeauthorizeStravaUsecase @Inject constructor(
    private val stravaTokenRepository: StravaTokenRepository,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState
) {
    suspend fun deauthorizeStrava() {
        val userAccountId = userAuthenticationState.getUserAccountId()
            ?: return

        val stravaToken = externalAppProvidersRepository.getStravaProviderToken(userAccountId)
            ?: return

        stravaTokenRepository.deauthorizeToken(stravaToken.accessToken)
        externalAppProvidersRepository.removeStravaProvider(userAccountId)
    }
}
