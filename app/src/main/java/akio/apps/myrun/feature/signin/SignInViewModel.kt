package akio.apps.myrun.feature.signin

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import androidx.lifecycle.LiveData

abstract class SignInViewModel : BaseViewModel() {
    abstract val signInSuccessResult: LiveData<Event<SignInSuccessResult>>
    abstract fun signInWithFacebookToken(tokenValue: String)
    abstract fun signInWithGoogleToken(googleIdToken: String)
}
