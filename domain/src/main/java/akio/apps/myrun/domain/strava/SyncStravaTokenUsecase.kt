package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.authentication.model.UserAccount
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import javax.inject.Inject

class SyncStravaTokenUsecase @Inject constructor(
    private val stravaTokenStorage: StravaTokenStorage,
    private val authenticationState: UserAuthenticationState,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository
) {
    suspend fun syncStravaToken(): ExternalAppToken.StravaToken? {
        val userAccount: UserAccount = authenticationState.getUserAccount()
            ?: return null

        val providers: ExternalProviders =
            externalAppProvidersRepository.getExternalProviders(userAccount.uid)

        val stravaToken: ExternalAppToken.StravaToken? = providers.strava?.token
        if (stravaToken == null) {
            stravaTokenStorage.clear()
        } else {
            stravaTokenStorage.setToken(stravaToken)
        }

        return stravaToken
    }
}
