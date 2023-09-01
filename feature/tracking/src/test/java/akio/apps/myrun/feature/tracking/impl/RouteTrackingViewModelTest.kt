package akio.apps.myrun.feature.tracking.impl

import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.tracking.api.LocationPresentConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus.STOPPED
import akio.apps.myrun.data.user.api.CurrentUserPreferences
import akio.apps.myrun.domain.tracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.tracking.StoreTrackingActivityDataUsecase
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegateImpl
import akio.apps.myrun.feature.tracking.RouteTrackingViewModel
import akio.apps.myrun.feature.tracking.model.RouteTrackingStats
import android.app.Application
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class RouteTrackingViewModelTest {
    private lateinit var mockedClearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase
    private lateinit var mockedRouteTrackingLocationRepository: RouteTrackingLocationRepository
    private lateinit var mockedRouteTrackingState: RouteTrackingState
    private lateinit var mockedStoreTrackingActivityDataUsecase: StoreTrackingActivityDataUsecase
    private lateinit var mockedAppContext: Application
    private lateinit var mockedLocationDataSource: LocationDataSource
    private lateinit var mockedRouteTrackingConfiguration: RouteTrackingConfiguration
    private lateinit var mockedCurrentUserPreferences: CurrentUserPreferences
    private lateinit var mockedLocationPresentConfig: LocationPresentConfiguration
    private lateinit var testee: RouteTrackingViewModel
    private lateinit var testCoroutineDispatcher: TestDispatcher

    @Before
    fun before() {
        mockedClearRouteTrackingStateUsecase = mock()
        mockedRouteTrackingLocationRepository = mock()
        mockedRouteTrackingState = mock()
        mockedStoreTrackingActivityDataUsecase = mock()
        mockedAppContext = mock()
        mockedLocationDataSource = mock()
        mockedRouteTrackingConfiguration = mock()
        mockedCurrentUserPreferences = mock()
        mockedLocationPresentConfig = mock()
        testCoroutineDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given tracking is stopped, when request initial data, then got initial location`() =
        runTest {
            whenever(mockedRouteTrackingState.getTrackingStatusFlow()).thenReturn(flowOf(STOPPED))

            testee = createViewModel()

            whenever(mockedRouteTrackingState.getTrackingStatus()).thenReturn(STOPPED)

            testee.requestInitialData()

            assertEquals(RouteTrackingStats(), testee.trackingStats.value)
            assertTrue(testee.trackingLocationUpdateValue.value.isEmpty())

            verify(mockedRouteTrackingState).getTrackingStatusFlow()
            verifyBlocking(mockedRouteTrackingState) { getTrackingStatus() }
        }

    private fun createViewModel() = RouteTrackingViewModel(
        mockedAppContext,
        mockedRouteTrackingState,
        mockedClearRouteTrackingStateUsecase,
        mockedStoreTrackingActivityDataUsecase,
        mockedRouteTrackingLocationRepository,
        mockedLocationDataSource,
        mockedRouteTrackingConfiguration,
        LaunchCatchingDelegateImpl(),
        mockedLocationPresentConfig,
        mockedCurrentUserPreferences,
        testCoroutineDispatcher
    )
}
