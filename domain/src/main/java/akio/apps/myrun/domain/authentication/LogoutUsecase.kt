package akio.apps.myrun.domain.authentication

import akio.apps.myrun.data.authentication.UserAuthenticationState
import javax.inject.Inject

class LogoutUsecase @Inject constructor(
    private val authenticationState: UserAuthenticationState
) {
    suspend fun logout() {
        authenticationState.clear()
    }
}
