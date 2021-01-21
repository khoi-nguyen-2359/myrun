package akio.apps.myrun.feature.userprofile.usecase

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.feature.userprofile.LogoutUsecase
import javax.inject.Inject

class FirebaseLogoutUsecase @Inject constructor(
    private val stravaTokenStorage: StravaTokenStorage,
    private val authenticationState: UserAuthenticationState
) : LogoutUsecase {
    override suspend fun logout() {
        stravaTokenStorage.clear()
        authenticationState.clear()
    }
}
