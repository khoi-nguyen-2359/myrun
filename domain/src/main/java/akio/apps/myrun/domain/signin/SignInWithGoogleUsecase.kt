package akio.apps.myrun.domain.signin

import akio.apps.myrun.data.authentication.SignInManager
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import javax.inject.Inject

class SignInWithGoogleUsecase @Inject constructor(
    private val signInManager: SignInManager
) {
    suspend fun signInWithGoogleToken(googleIdToken: String): SignInSuccessResult =
        signInManager.signInGoogle(googleIdToken)
}
