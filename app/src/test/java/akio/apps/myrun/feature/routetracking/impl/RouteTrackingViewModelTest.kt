package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.InstantTaskExecutorTest
import akio.apps.myrun.data.LaunchCatchingDelegateImpl
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.RouteTrackingStatus.STOPPED
import akio.apps.myrun.domain.routetracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.routetracking.GetTrackedLocationsUsecase
import akio.apps.myrun.domain.routetracking.StoreTrackingActivityDataUsecase
import akio.apps.test.whenBlocking
import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

@ExperimentalCoroutinesApi
class RouteTrackingViewModelTest : InstantTaskExecutorTest() {

    @Mock
    lateinit var mockedAuthenticationState: UserAuthenticationState

    @Mock
    lateinit var mockedExternalAppProvidersRepository: ExternalAppProvidersRepository

    @Mock
    lateinit var mockedClearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase

    @Mock
    lateinit var mockedRouteTrackingState: RouteTrackingState

    @Mock
    lateinit var mockedGetTrackedLocationsUsecase: GetTrackedLocationsUsecase

    @Mock
    lateinit var mockedStoreTrackingActivityDataUsecase: StoreTrackingActivityDataUsecase

    @Mock
    lateinit var mockedAppContext: Application

    @Mock
    lateinit var mockedLocationDataSource: LocationDataSource

    @Mock
    lateinit var mockedRouteTrackingConfiguration: RouteTrackingConfiguration

    lateinit var testee: RouteTrackingViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `given tracking is stopped, when request initial data, then got initial location`() {
        whenBlocking(mockedRouteTrackingState) {
            getTrackingStatusFlow()
        }.thenReturn(flowOf(STOPPED))

        testee = createViewModel()

        whenBlocking(mockedRouteTrackingState) {
            getTrackingStatus()
        }.thenReturn(STOPPED)

        testee.requestInitialData()

        assertNull(testee.trackingStats.value)
        assertNull(testee.trackingLocationBatch.value)

        verify(mockedRouteTrackingState).getTrackingStatusFlow()
        verifyBlocking(mockedRouteTrackingState) { getTrackingStatus() }
    }

    private fun createViewModel() = RouteTrackingViewModel(
        mockedAppContext,
        mockedGetTrackedLocationsUsecase,
        mockedRouteTrackingState,
        mockedClearRouteTrackingStateUsecase,
        mockedStoreTrackingActivityDataUsecase,
        mockedExternalAppProvidersRepository,
        mockedAuthenticationState,
        mockedLocationDataSource,
        mockedRouteTrackingConfiguration,
        LaunchCatchingDelegateImpl()
    )
}
