package akio.apps.myrun.feature.signin.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import akio.apps.myrun.domain.authentication.SignInWithFacebookUsecase
import akio.apps.myrun.domain.authentication.SignInWithGoogleUsecase
import akio.apps.myrun.domain.authentication.SyncUserProfileUsecase
import akio.apps.myrun.feature.signin.SignInViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject

class SignInViewModelImpl @Inject constructor(
    private val signInWithFacebookUsecase: SignInWithFacebookUsecase,
    private val signInWithGoogleUsecase: SignInWithGoogleUsecase,
    private val signInWithPhoneUsecase: FirebaseSignInWithPhoneUsecase,
    private val syncUserProfileUsecase: SyncUserProfileUsecase
) : SignInViewModel() {

    private val _signInSuccessResult = MutableLiveData<Event<SignInSuccessResult>>()
    override val signInSuccessResult: LiveData<Event<SignInSuccessResult>> = _signInSuccessResult

    override fun signInWithFirebasePhoneCredential(phoneAuthCredential: PhoneAuthCredential) {
        launchCatching {
            val result =
                signInWithPhoneUsecase.signInWithFirebasePhoneCredential(phoneAuthCredential)
            onSignInSuccess(result)
        }
    }

    private suspend fun onSignInSuccess(result: SignInSuccessResult) {
        if (result.isNewUser) {
            syncUserProfileUsecase.syncUserProfile()
        }
        _signInSuccessResult.value = Event(result)
    }

    override fun signInWithFacebookToken(tokenValue: String) {
        launchCatching {
            val result = signInWithFacebookUsecase.signInWithFacebookAccessToken(tokenValue)
            onSignInSuccess(result)
        }
    }

    override fun signInWithGoogleToken(googleIdToken: String) {
        launchCatching {
            val result = signInWithGoogleUsecase.signInWithGoogleToken(googleIdToken)
            onSignInSuccess(result)
        }
    }
}
