package akio.apps.myrun.feature.signin.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.feature.signin.SignInViewModel
import akio.apps.myrun.feature.signin.SignInWithFacebookUsecase
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject

class SignInViewModelImpl @Inject constructor(
    private val signInWithFacebookUsecase: SignInWithFacebookUsecase,
    private val signInWithPhoneUsecase: FirebaseSignInWithPhoneUsecase
) : SignInViewModel() {

    private val _signInSuccessResult = MutableLiveData<Event<SignInSuccessResult>>()
    override val signInSuccessResult: LiveData<Event<SignInSuccessResult>> = _signInSuccessResult

    override fun signInWithFirebasePhoneCredential(phoneAuthCredential: PhoneAuthCredential) {
        launchCatching {
            val result = signInWithPhoneUsecase.signInWithFirebasePhoneCredential(phoneAuthCredential)
            _signInSuccessResult.value = Event(result)
        }
    }

    override fun signInWithFacebookToken(tokenValue: String) {
        launchCatching {
            val result = signInWithFacebookUsecase.signInWithFacebookAccessToken(tokenValue)
            _signInSuccessResult.value = Event(result)
        }
    }


}