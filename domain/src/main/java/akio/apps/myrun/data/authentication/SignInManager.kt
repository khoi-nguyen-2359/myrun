package akio.apps.myrun.data.authentication

import akio.apps.myrun.data.authentication.model.SignInSuccessResult

interface SignInManager {
    suspend fun linkFacebook(facebookAccessToken: String)
    suspend fun signInFacebook(facebookAccessToken: String): SignInSuccessResult
    suspend fun signInGoogle(googleIdToken: String): SignInSuccessResult
}
