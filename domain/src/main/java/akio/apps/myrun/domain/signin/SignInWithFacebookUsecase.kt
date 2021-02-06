package akio.apps.myrun.domain.signin

import akio.apps.myrun.data.authentication.SignInManager
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import javax.inject.Inject

class SignInWithFacebookUsecase @Inject constructor(
    private val signInManager: SignInManager
) {
    suspend fun signInWithFacebookAccessToken(tokenValue: String): SignInSuccessResult =
        signInManager.signInFacebook(tokenValue)
}
