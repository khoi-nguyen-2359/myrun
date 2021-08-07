package akio.apps.myrun.feature.signin.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import akio.apps.myrun.domain.authentication.PostSigningInUsecase
import akio.apps.myrun.domain.authentication.SignInWithFacebookUsecase
import akio.apps.myrun.domain.authentication.SignInWithGoogleUsecase
import akio.apps.myrun.feature.signin.SignInViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class SignInViewModelImpl @Inject constructor(
    private val signInWithFacebookUsecase: SignInWithFacebookUsecase,
    private val signInWithGoogleUsecase: SignInWithGoogleUsecase,
    private val postSigningInUsecase: PostSigningInUsecase
) : SignInViewModel() {

    private val _signInSuccessResult = MutableLiveData<Event<SignInSuccessResult>>()
    override val signInSuccessResult: LiveData<Event<SignInSuccessResult>> = _signInSuccessResult

    private suspend fun onSignInSuccess(result: SignInSuccessResult) {
        postSigningInUsecase.invoke(result)
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
