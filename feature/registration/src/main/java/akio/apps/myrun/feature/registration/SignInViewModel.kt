package akio.apps.myrun.feature.registration

import akio.apps.myrun.data.authentication.api.SignInManager
import akio.apps.myrun.data.authentication.api.model.SignInSuccessResult
import akio.apps.myrun.domain.launchcatching.Event
import akio.apps.myrun.domain.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.domain.user.PostSignInUsecase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class SignInViewModel @Inject constructor(
    private val signInManager: SignInManager,
    private val postSignInUsecase: PostSignInUsecase,
    private val launchCatching: LaunchCatchingDelegate,
) : ViewModel(), LaunchCatchingDelegate by launchCatching {

    private val _signInSuccessResult = MutableStateFlow<Event<SignInSuccessResult>>(Event())
    val signInSuccessResult: Flow<Event<SignInSuccessResult>> = _signInSuccessResult

    private suspend fun onSignInSuccess(result: SignInSuccessResult) {
        postSignInUsecase.invoke(result)
        _signInSuccessResult.value = Event(result)
    }

    fun signInWithFacebookToken(tokenValue: String) {
        viewModelScope.launchCatching {
            val result = signInManager.signInFacebook(tokenValue)
            onSignInSuccess(result)
        }
    }

    fun signInWithGoogleToken(googleIdToken: String) {
        viewModelScope.launchCatching {
            val result = signInManager.signInGoogle(googleIdToken)
            onSignInSuccess(result)
        }
    }
}
