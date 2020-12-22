package akio.apps.myrun.feature.signin

import akio.apps.myrun.feature.signin.impl.SignInSuccessResult

interface SignInWithGoogleUsecase {
    suspend fun signInWithGoogleToken(googleIdToken: String): SignInSuccessResult
}