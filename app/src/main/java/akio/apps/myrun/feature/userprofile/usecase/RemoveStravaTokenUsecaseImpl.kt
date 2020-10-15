package akio.apps.myrun.feature.userprofile.usecase

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.feature.userprofile.RemoveStravaTokenUsecase
import javax.inject.Inject

class RemoveStravaTokenUsecaseImpl @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val stravaTokenStorage: StravaTokenStorage
) : RemoveStravaTokenUsecase {
    override suspend fun removeStravaTokenUsecase() {
        val accountId = userAuthenticationState.getUserAccountId()
            ?: return

        externalAppProvidersRepository.removeStravaProvider(accountId)
        stravaTokenStorage.clear()
    }
}