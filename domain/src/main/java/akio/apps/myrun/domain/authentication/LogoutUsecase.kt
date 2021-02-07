package akio.apps.myrun.domain.authentication

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import javax.inject.Inject

class LogoutUsecase @Inject constructor(
    private val stravaTokenStorage: StravaTokenStorage,
    private val authenticationState: UserAuthenticationState
) {
    suspend fun logout() {
        stravaTokenStorage.clear()
        authenticationState.clear()
    }
}
