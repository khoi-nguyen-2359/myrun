package akio.apps.myrun.feature.splash.impl

import akio.apps.base.feature.lifecycle.Event
import akio.apps.base.feature.viewmodel.LaunchCatchingDelegate
import akio.apps.base.feature.viewmodel.LaunchCatchingDelegateImpl
import akio.apps.myrun.data.authentication.UserAuthenticationState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class SplashViewModel @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val launchCatchingDelegate: LaunchCatchingDelegateImpl = LaunchCatchingDelegateImpl()
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    private val _isUserSignedIn = MutableStateFlow<Event<Boolean>?>(null)
    val isUserSignedIn: Flow<Event<Boolean>> = _isUserSignedIn.filterNotNull()

    init {
        viewModelScope.launchCatching {
            val isSignedIn = userAuthenticationState.isSignedIn()
            _isUserSignedIn.value = Event(isSignedIn)
        }
    }
}
