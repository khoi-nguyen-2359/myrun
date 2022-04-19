package akio.apps.myrun.data.authentication.impl

import akio.apps.myrun.data.authentication.api.SignInManager
import akio.apps.myrun.data.authentication.api.model.SignInMethod
import akio.apps.myrun.data.authentication.api.model.SignInSuccessResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseSignInManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : SignInManager {
    override suspend fun linkFacebook(facebookAccessToken: String) {
        val credential = FacebookAuthProvider.getCredential(facebookAccessToken)
        firebaseAuth.currentUser?.linkWithCredential(credential)?.await()
    }

    override suspend fun signInFacebook(facebookAccessToken: String): SignInSuccessResult {
        val credential = FacebookAuthProvider.getCredential(facebookAccessToken)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        return SignInSuccessResult(
            authResult.additionalUserInfo?.isNewUser ?: false,
            SignInMethod.Facebook
        )
    }

    override suspend fun signInGoogle(googleIdToken: String): SignInSuccessResult {
        val googleAuthCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val signInResult = firebaseAuth.signInWithCredential(googleAuthCredential).await()
        return SignInSuccessResult(
            signInResult.additionalUserInfo?.isNewUser ?: false,
            SignInMethod.Google
        )
    }
}
