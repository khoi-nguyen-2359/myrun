package akio.apps.myrun.feature.splash.impl

import akio.apps.myrun.data.LaunchCatchingDelegate
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class SplashViewModel @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    private val _isUserSignedIn = MutableStateFlow<akio.apps.myrun.data.Event<Boolean>?>(null)
    val isUserSignedIn: Flow<akio.apps.myrun.data.Event<Boolean>> = _isUserSignedIn.filterNotNull()

    init {
        viewModelScope.launchCatching {
            val isSignedIn = userAuthenticationState.isSignedIn()
            _isUserSignedIn.value = akio.apps.myrun.data.Event(isSignedIn)
        }
    }
}
