package akio.apps.myrun.feature.userprofile.usecase

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.feature.userprofile.LogoutUsecase
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FirebaseLogoutUsecase @Inject constructor(
    private val stravaTokenStorage: StravaTokenStorage,
    private val authenticationState: UserAuthenticationState
) : LogoutUsecase {
    override fun logout() {
        stravaTokenStorage.clear()
        authenticationState.clear()
    }
}