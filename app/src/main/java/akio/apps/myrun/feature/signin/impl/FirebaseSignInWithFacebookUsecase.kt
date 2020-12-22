package akio.apps.myrun.feature.signin.impl

import akio.apps.myrun.feature.signin.SignInWithFacebookUsecase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseSignInWithFacebookUsecase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : SignInWithFacebookUsecase {
    override suspend fun signInWithFacebookAccessToken(tokenValue: String): SignInSuccessResult {
        val credential = FacebookAuthProvider.getCredential(tokenValue)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        return SignInSuccessResult(
            authResult.additionalUserInfo?.isNewUser ?: false,
            SignInMethod.Facebook
        )
    }
}