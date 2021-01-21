package akio.apps.myrun.feature.signin.impl

import akio.apps.myrun.feature.signin.SignInWithGoogleUsecase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SignInWithGoogleUsecaseImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : SignInWithGoogleUsecase {
    override suspend fun signInWithGoogleToken(googleIdToken: String): SignInSuccessResult {
        val googleAuthCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val signInResult = firebaseAuth.signInWithCredential(googleAuthCredential)
            .await()
        return SignInSuccessResult(
            signInResult.additionalUserInfo?.isNewUser ?: false,
            SignInMethod.Google
        )
    }
}
