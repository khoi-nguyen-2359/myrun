package akio.apps.myrun.feature.splash.usecase

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.feature.splash.SignOutAnonymousUserUsecase
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class SignOutAnonymousUserUsecaseImpl @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState
): SignOutAnonymousUserUsecase {

    override fun signOutAnonymousUser() {
        if (userAuthenticationState.isAnonymousUser()) {
            userAuthenticationState.clear()
        }
    }
}