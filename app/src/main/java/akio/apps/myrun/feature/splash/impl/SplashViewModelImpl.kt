package akio.apps.myrun.feature.splash.impl

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.feature.splash.SignOutAnonymousUserUsecase
import akio.apps.myrun.feature.splash.SplashViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class SplashViewModelImpl @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
    private val userAuthenticationState: UserAuthenticationState,
    private val signOutAnonymousUserUsecase: SignOutAnonymousUserUsecase
) : SplashViewModel() {

    private val _isRouteTrackingInProgress = MutableLiveData<Boolean>()
    override val isRouteTrackingInProgress: LiveData<Boolean> = _isRouteTrackingInProgress

    private val _isUserSignedIn = MutableLiveData<Boolean>()
    override val isUserSignedIn: LiveData<Boolean> = _isUserSignedIn

    init {
        // Ensure remove all obsolete logic of anonymous user
        signOutAnonymousUserUsecase.signOutAnonymousUser()

        launchCatching {
            val isSignedIn = userAuthenticationState.isSignedIn()
            if (isSignedIn) {
                _isRouteTrackingInProgress.value = routeTrackingState.getTrackingStatus() != RouteTrackingStatus.STOPPED
            }
            _isUserSignedIn.value = isSignedIn
        }
    }
}