package akio.apps.myrun.data.authentication.api

import akio.apps.myrun.data.authentication.api.model.SignInSuccessResult

interface SignInManager {
    suspend fun linkFacebook(facebookAccessToken: String)
    suspend fun signInFacebook(facebookAccessToken: String): SignInSuccessResult
    suspend fun signInGoogle(googleIdToken: String): SignInSuccessResult
}
