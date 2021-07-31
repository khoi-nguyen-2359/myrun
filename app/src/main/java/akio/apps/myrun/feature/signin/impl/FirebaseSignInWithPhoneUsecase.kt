package akio.apps.myrun.feature.signin.impl

import akio.apps.myrun.data.authentication.model.SignInMethod
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseSignInWithPhoneUsecase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun signInWithFirebasePhoneCredential(
        phoneAuthCredential: PhoneAuthCredential
    ): SignInSuccessResult {
        val authResult = firebaseAuth.signInWithCredential(phoneAuthCredential)
            .await()
        return SignInSuccessResult(
            authResult.additionalUserInfo?.isNewUser ?: false,
            SignInMethod.Phone
        )
    }
}
