package akio.apps.myrun.feature.splash

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import app.cash.turbine.test
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class SplashViewModelTest {

    private lateinit var splashViewModel: SplashViewModel
    private lateinit var mockedUserAuthState: UserAuthenticationState
    private lateinit var mockLaunchCatchingDelegate: LaunchCatchingDelegate

    @Before
    fun setup() {
        mockedUserAuthState = mock()
        mockLaunchCatchingDelegate = mock()
    }

    @Test
    fun testIsUserSignedIn_ErrorCase() = runBlockingTest {
        val userAuthStateError = Exception("User auth state error!")
        whenever(mockedUserAuthState.isSignedIn())
            .then { throw userAuthStateError }

        splashViewModel = SplashViewModel(mockedUserAuthState, mockLaunchCatchingDelegate)
        splashViewModel.isUserSignedIn.test {
            awaitComplete()
            verify(mockLaunchCatchingDelegate).setLaunchCatchingError(userAuthStateError)
        }
    }
}
