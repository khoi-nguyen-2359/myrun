package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.InstantTaskExecutorTest
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus.STOPPED
import akio.apps.myrun.domain.routetracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.routetracking.GetTrackedLocationsUsecase
import akio.apps.myrun.domain.routetracking.StoreTrackingActivityDataUsecase
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.test.wheneverBlocking
import android.app.Application
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class RouteTrackingViewModelImplTest : InstantTaskExecutorTest() {
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

    lateinit var testee: RouteTrackingViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `given tracking is stopped, when request initial data, then got initial location`() {
        wheneverBlocking(mockedRouteTrackingState) {
            getTrackingStatusFlow()
        }.thenReturn(flowOf(STOPPED))

        testee = createViewModel()

        wheneverBlocking(mockedRouteTrackingState) { getTrackingStatus() }
            .thenReturn(STOPPED)

        testee.requestInitialData()

        assertNull(testee.trackingStats.value)
        assertNull(testee.trackingLocationBatch.value)

        verify(mockedRouteTrackingState).getTrackingStatusFlow()
        verifyBlocking(mockedRouteTrackingState) { getTrackingStatus() }
    }

    private fun createViewModel() = RouteTrackingViewModelImpl(
        mockedAppContext,
        mockedGetTrackedLocationsUsecase,
        mockedRouteTrackingState,
        mockedClearRouteTrackingStateUsecase,
        mockedStoreTrackingActivityDataUsecase,
        mockedExternalAppProvidersRepository,
        mockedAuthenticationState,
        mockedLocationDataSource
    )
}
