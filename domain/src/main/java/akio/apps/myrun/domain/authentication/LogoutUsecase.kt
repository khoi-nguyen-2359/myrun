package akio.apps.myrun.domain.authentication

import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.authentication.UserAuthenticationState
import javax.inject.Inject

class LogoutUsecase @Inject constructor(
    private val authenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage
) {
    suspend fun logout() {
        activityLocalStorage.clearAll()
        authenticationState.clear()
    }
}
