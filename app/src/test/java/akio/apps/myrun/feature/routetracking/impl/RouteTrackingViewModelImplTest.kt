package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.InstantTaskExecutorTest
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.RouteTrackingStatus.STOPPED
import akio.apps.myrun.data.routetracking.model.LatLng
import akio.apps.myrun.domain.routetracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.routetracking.GetMapInitialLocationUsecase
import akio.apps.myrun.domain.routetracking.GetTrackedLocationsUsecase
import akio.apps.myrun.domain.routetracking.SaveRouteTrackingActivityUsecase
import akio.apps.myrun.domain.strava.ExportTrackingActivityToStravaFileUsecase
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.usertimeline.model.ActivityEntityMapper
import akio.apps.test.wheneverBlocking
import android.content.Context
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
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
    lateinit var mockedActivityMapper: ActivityEntityMapper

    @Mock
    lateinit var mockedExportActivityToStravaFileUsecase: ExportTrackingActivityToStravaFileUsecase

    @Mock
    lateinit var mockedClearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase

    @Mock
    lateinit var mockedSaveRouteTrackingActivityUsecase: SaveRouteTrackingActivityUsecase

    @Mock
    lateinit var mockedRouteTrackingState: RouteTrackingState

    @Mock
    lateinit var mockedGetTrackedLocationsUsecase: GetTrackedLocationsUsecase

    @Mock
    lateinit var mockedGetMapInitialLocationUsecase: GetMapInitialLocationUsecase

    @Mock
    lateinit var mockedAppContext: Context

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

        val mockedLatLng = LatLng(10.0, 20.0)
        wheneverBlocking(mockedGetMapInitialLocationUsecase) { getMapInitialLocation() }
            .thenReturn(mockedLatLng)
        wheneverBlocking(mockedRouteTrackingState) { getTrackingStatus() }
            .thenReturn(STOPPED)

        testee.requestInitialData()

        assertEquals(mockedLatLng, testee.mapInitialLocation.value?.peekContent())
        assertNull(testee.trackingStats.value)
        assertNull(testee.trackingLocationBatch.value)

        verify(mockedRouteTrackingState).getTrackingStatusFlow()
        verifyBlocking(mockedGetMapInitialLocationUsecase) { getMapInitialLocation() }
        verifyBlocking(mockedRouteTrackingState) { getTrackingStatus() }
    }

    private fun createViewModel() = RouteTrackingViewModelImpl(
        mockedAppContext,
        mockedGetMapInitialLocationUsecase,
        mockedGetTrackedLocationsUsecase,
        mockedRouteTrackingState,
        mockedSaveRouteTrackingActivityUsecase,
        mockedClearRouteTrackingStateUsecase,
        mockedExportActivityToStravaFileUsecase,
        mockedActivityMapper,
        mockedExternalAppProvidersRepository,
        mockedAuthenticationState
    )
}

