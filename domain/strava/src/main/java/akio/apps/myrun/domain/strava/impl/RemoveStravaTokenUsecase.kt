package akio.apps.myrun.domain.strava.impl

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import javax.inject.Inject

class RemoveStravaTokenUsecase @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState,
) {
    suspend fun removeStravaToken() {
        val accountId = userAuthenticationState.getUserAccountId()
            ?: return

        externalAppProvidersRepository.removeStravaProvider(accountId)
    }
}
