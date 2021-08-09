package akio.apps.myrun.feature.registration

import akio.apps.base.feature.lifecycle.Event
import akio.apps.base.feature.viewmodel.LaunchCatchingDelegate
import akio.apps.base.feature.viewmodel.LaunchCatchingDelegateImpl
import akio.apps.myrun.data.authentication.SignInManager
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import akio.apps.myrun.domain.authentication.PostSignInUsecase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class SignInViewModel @Inject constructor(
    private val signInManager: SignInManager,
    private val postSignInUsecase: PostSignInUsecase,
    private val launchCatching: LaunchCatchingDelegateImpl
) : ViewModel(), LaunchCatchingDelegate by launchCatching {

    private val _signInSuccessResult = MutableStateFlow<Event<SignInSuccessResult>?>(null)
    val signInSuccessResult: Flow<Event<SignInSuccessResult>> = _signInSuccessResult.filterNotNull()

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