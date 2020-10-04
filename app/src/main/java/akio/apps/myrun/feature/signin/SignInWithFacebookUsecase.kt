package akio.apps.myrun.feature.signin

import akio.apps.myrun.feature.signin.impl.SignInSuccessResult

interface SignInWithFacebookUsecase {
    suspend fun signInWithFacebookAccessToken(tokenValue: String): SignInSuccessResult
}