package akio.apps.myrun.feature.splash.impl

import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.feature.splash.SignInAnonymouslyUsecase
import akio.apps.myrun.feature.splash.SplashViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModelImpl @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
    private val signInAnonymouslyUsecase: SignInAnonymouslyUsecase
) : SplashViewModel() {

    private val _isRouteTrackingInProgress = MutableLiveData<Boolean>()
    override val isRouteTrackingInProgress: LiveData<Boolean> = _isRouteTrackingInProgress

    init {
        launchCatching {
            signInAnonymouslyUsecase.signInAnonymously()
            _isRouteTrackingInProgress.value = routeTrackingState.getTrackingStatus() != RouteTrackingStatus.STOPPED
        }
    }
}