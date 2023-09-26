package akio.apps.myrun.feature.splash

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.feature.main.MainViewModel
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var mockedUserAuthState: UserAuthenticationState
    private lateinit var mockLaunchCatchingDelegate: LaunchCatchingDelegate

    @Before
    fun setup() {
        mockedUserAuthState = mock()
        mockLaunchCatchingDelegate = mock()
    }

    @Test
    fun testIsUserSignedIn_ErrorCase() = runTest {
        val userAuthStateError = Exception("User auth state error!")
        whenever(mockedUserAuthState.isSignedIn())
            .then { throw userAuthStateError }

        mainViewModel = MainViewModel(mockedUserAuthState, mockLaunchCatchingDelegate)
        mainViewModel.isUserSignedIn.test {
            awaitComplete()
            verify(mockLaunchCatchingDelegate).setLaunchCatchingError(userAuthStateError)
        }
    }
}
