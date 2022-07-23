package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaSyncState
import akio.apps.myrun.data.eapps.api.StravaTokenRepository
import javax.inject.Inject

class UpdateStravaTokenUsecase @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val stravaTokenRepository: StravaTokenRepository,
    private val stravaSyncState: StravaSyncState,
    private val userAuthenticationState: UserAuthenticationState,
) {
    suspend fun updateStravaToken(stravaLoginCode: String) {
        val accountId = userAuthenticationState.getUserAccountId()
            ?: return
        val stravaToken = stravaTokenRepository.exchangeToken(stravaLoginCode)

        externalAppProvidersRepository.updateStravaProvider(accountId, stravaToken)

        stravaSyncState.setStravaSyncAccountId(accountId)
    }
}
