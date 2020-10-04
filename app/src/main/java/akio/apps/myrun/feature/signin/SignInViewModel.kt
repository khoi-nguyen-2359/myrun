package akio.apps.myrun.feature.signin

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.feature.signin.impl.SignInSuccessResult
import androidx.lifecycle.LiveData
import com.google.firebase.auth.PhoneAuthCredential

abstract class SignInViewModel : BaseViewModel() {
    abstract val signInSuccessResult: LiveData<Event<SignInSuccessResult>>
    abstract fun signInWithFirebasePhoneCredential(phoneAuthCredential: PhoneAuthCredential)
    abstract fun signInWithFacebookToken(tokenValue: String)
}