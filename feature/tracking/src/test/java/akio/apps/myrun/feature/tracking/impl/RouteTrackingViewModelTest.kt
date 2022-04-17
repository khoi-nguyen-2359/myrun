package akio.apps.myrun.feature.tracking.impl

import akio.apps._base.InstantTaskExecutorTest
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus.STOPPED
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegateImpl
import akio.apps.myrun.domain.tracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.tracking.StoreTrackingActivityDataUsecase
import akio.apps.myrun.feature.tracking.RouteTrackingViewModel
import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class RouteTrackingViewModelTest : InstantTaskExecutorTest() {
    private lateinit var mockedAuthenticationState: UserAuthenticationState
    private lateinit var mockedExternalAppProvidersRepository: ExternalAppProvidersRepository
    private lateinit var mockedClearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase
    private lateinit var mockedRouteTrackingLocationRepository: RouteTrackingLocationRepository
    private lateinit var mockedRouteTrackingState: RouteTrackingState
    private lateinit var mockedStoreTrackingActivityDataUsecase: StoreTrackingActivityDataUsecase
    private lateinit var mockedAppContext: Application
    private lateinit var mockedLocationDataSource: LocationDataSource
    private lateinit var mockedRouteTrackingConfiguration: RouteTrackingConfiguration
    private lateinit var testee: RouteTrackingViewModel

    @Before
    fun setup() {
        mockedAuthenticationState = mock()
        mockedExternalAppProvidersRepository = mock()
        mockedClearRouteTrackingStateUsecase = mock()
        mockedRouteTrackingLocationRepository = mock()
        mockedRouteTrackingState = mock()
        mockedStoreTrackingActivityDataUsecase = mock()
        mockedAppContext = mock()
        mockedLocationDataSource = mock()
        mockedRouteTrackingConfiguration = mock()
    }

    @Test
    fun `given tracking is stopped, when request initial data, then got initial location`() =
        runBlockingTest {
            whenever(mockedRouteTrackingState.getTrackingStatusFlow()).thenReturn(flowOf(STOPPED))

            testee = createViewModel()

            whenever(mockedRouteTrackingState.getTrackingStatus()).thenReturn(STOPPED)

            testee.requestInitialData()

            assertNull(testee.trackingStats.value)
            assertNull(testee.trackingLocationBatch.value)

            verify(mockedRouteTrackingState).getTrackingStatusFlow()
            verifyBlocking(mockedRouteTrackingState) { getTrackingStatus() }
        }

    private fun createViewModel() = RouteTrackingViewModel(
        mockedAppContext,
        mockedRouteTrackingState,
        mockedClearRouteTrackingStateUsecase,
        mockedStoreTrackingActivityDataUsecase,
        mockedExternalAppProvidersRepository,
        mockedRouteTrackingLocationRepository,
        mockedAuthenticationState,
        mockedLocationDataSource,
        mockedRouteTrackingConfiguration,
        LaunchCatchingDelegateImpl()
    )
}
