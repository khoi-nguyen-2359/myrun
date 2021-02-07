package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import javax.inject.Inject

class RemoveStravaTokenUsecase @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val stravaTokenStorage: StravaTokenStorage
) {
    suspend fun removeStravaToken() {
        val accountId = userAuthenticationState.getUserAccountId()
            ?: return

        externalAppProvidersRepository.removeStravaProvider(accountId)
        stravaTokenStorage.clear()
    }
}
