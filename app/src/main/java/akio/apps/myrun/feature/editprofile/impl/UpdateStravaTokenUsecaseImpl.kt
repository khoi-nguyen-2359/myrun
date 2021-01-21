package akio.apps.myrun.feature.editprofile.impl

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.feature.editprofile.UpdateStravaTokenUsecase
import javax.inject.Inject

class UpdateStravaTokenUsecaseImpl @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val stravaTokenStorage: StravaTokenStorage
) : UpdateStravaTokenUsecase {
    override suspend fun updateStravaToken(stravaToken: ExternalAppToken.StravaToken) {
        val accountId = userAuthenticationState.getUserAccountId()
            ?: return

        externalAppProvidersRepository.updateStravaProvider(accountId, stravaToken)
        stravaTokenStorage.setToken(stravaToken)
    }
}
