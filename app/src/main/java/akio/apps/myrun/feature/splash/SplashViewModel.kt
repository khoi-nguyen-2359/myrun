package akio.apps.myrun.feature.splash

import akio.apps._base.viewmodel.BaseViewModel
import androidx.lifecycle.LiveData

abstract class SplashViewModel: BaseViewModel() {
    abstract val isRouteTrackingInProgress: LiveData<Boolean>
    abstract val isUserSignedIn: LiveData<Boolean>
}