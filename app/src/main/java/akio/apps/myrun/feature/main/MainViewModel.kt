package akio.apps.myrun.feature.main

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class MainViewModel @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    val isUserSignedIn: Flow<Boolean> = flow {
        emit(userAuthenticationState.isSignedIn())
    }
        .catch { exception ->
            setLaunchCatchingError(exception)
        }
}
