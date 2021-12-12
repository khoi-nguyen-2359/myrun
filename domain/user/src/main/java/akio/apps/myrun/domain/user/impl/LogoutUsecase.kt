package akio.apps.myrun.domain.user.impl

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.domain.activity.api.ActivityLocalStorage
import javax.inject.Inject

class LogoutUsecase @Inject constructor(
    private val authenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
) {
    suspend fun logout() {
        activityLocalStorage.clearAll()
        authenticationState.clear()
    }
}
