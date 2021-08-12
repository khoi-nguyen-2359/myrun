package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import javax.inject.Inject

class UpdateStravaTokenUsecase @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState,
) {
    suspend fun updateStravaToken(stravaToken: ExternalAppToken.StravaToken) {
        val accountId = userAuthenticationState.getUserAccountId()
            ?: return

        externalAppProvidersRepository.updateStravaProvider(accountId, stravaToken)
    }
}
