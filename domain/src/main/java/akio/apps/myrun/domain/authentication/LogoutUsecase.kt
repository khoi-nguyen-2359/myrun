package akio.apps.myrun.domain.authentication

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.authentication.UserAuthenticationState
import javax.inject.Inject

class LogoutUsecase @Inject constructor(
    private val authenticationState: UserAuthenticationState,
    private val activityLocalStorage: akio.apps.myrun.data.activity.api.ActivityLocalStorage
) {
    suspend fun logout() {
        activityLocalStorage.clearAll()
        authenticationState.clear()
    }
}
