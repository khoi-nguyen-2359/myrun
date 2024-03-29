package akio.apps.myrun.feature.registration

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.authentication.api.SignInManager
import akio.apps.myrun.data.authentication.api.model.SignInSuccessResult
import akio.apps.myrun.domain.user.PostSignInUsecase
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext

internal class SignInViewModel @Inject constructor(
    private val signInManager: SignInManager,
    private val postSignInUsecase: PostSignInUsecase,
    private val launchCatching: LaunchCatchingDelegate,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), LaunchCatchingDelegate by launchCatching {

    private val _signInSuccessResult = MutableSharedFlow<SignInSuccessResult>()
    val signInSuccessResult: Flow<SignInSuccessResult> = _signInSuccessResult

    private val _reAuthSuccessResult = MutableSharedFlow<Unit>()
    val reAuthSuccessResult: Flow<Unit> = _reAuthSuccessResult

    private suspend fun onSignInSuccess(result: SignInSuccessResult) = withContext(ioDispatcher) {
        postSignInUsecase.invoke(result)
        _signInSuccessResult.emit(result)
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

    fun reAuthWithGoogleToken(googleIdToken: String) {
        viewModelScope.launchCatching {
            signInManager.reAuthGoogle(googleIdToken)
            _reAuthSuccessResult.emit(Unit)
        }
    }
}
