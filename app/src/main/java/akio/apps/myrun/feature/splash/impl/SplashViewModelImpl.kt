package akio.apps.myrun.feature.splash.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.feature.splash.SignOutAnonymousUserUsecase
import akio.apps.myrun.feature.splash.SplashViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class SplashViewModelImpl @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val signOutAnonymousUserUsecase: SignOutAnonymousUserUsecase
) : SplashViewModel() {

    private val _isUserSignedIn = MutableLiveData<Event<Boolean>>()
    override val isUserSignedIn: LiveData<Event<Boolean>> = _isUserSignedIn

    init {
        // Ensure remove all obsolete logic of anonymous user
        signOutAnonymousUserUsecase.signOutAnonymousUser()

        launchCatching {
            val isSignedIn = userAuthenticationState.isSignedIn()
            _isUserSignedIn.value = Event(isSignedIn)
        }
    }
}