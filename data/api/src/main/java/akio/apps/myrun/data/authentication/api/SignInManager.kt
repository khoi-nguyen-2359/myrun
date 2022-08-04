package akio.apps.myrun.data.authentication.api

import akio.apps.myrun.data.authentication.api.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.api.model.SignInSuccessResult
import kotlin.jvm.Throws

interface SignInManager {
    suspend fun linkFacebook(facebookAccessToken: String)
    suspend fun signInFacebook(facebookAccessToken: String): SignInSuccessResult
    suspend fun signInGoogle(googleIdToken: String): SignInSuccessResult
    suspend fun reAuthGoogle(googleIdToken: String)

    @Throws(UnauthorizedUserError::class)
    suspend fun deleteUserAccount()
}
