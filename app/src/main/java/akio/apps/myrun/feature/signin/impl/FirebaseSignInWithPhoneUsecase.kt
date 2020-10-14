package akio.apps.myrun.feature.signin.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseSignInWithPhoneUsecase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun signInWithFirebasePhoneCredential(phoneAuthCredential: PhoneAuthCredential): SignInSuccessResult {
        val authResult = firebaseAuth.signInWithCredential(phoneAuthCredential).await()
        return SignInSuccessResult(authResult.additionalUserInfo?.isNewUser ?: false, SignInMethod.Phone)
    }
}