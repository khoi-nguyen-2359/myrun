package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.InstantTaskExecutorTest
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus.STOPPED
import akio.apps.myrun.domain.activityexport.ClearExportActivityLocationUsecase
import akio.apps.myrun.domain.activityexport.SaveExportActivityLocationsUsecase
import akio.apps.myrun.domain.routetracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.routetracking.GetTrackedLocationsUsecase
import akio.apps.myrun.domain.routetracking.UploadActivityUsecase
import akio.apps.myrun.domain.strava.ExportTrackingActivityToStravaFileUsecase
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
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
    lateinit var mockedActivityMapper: ActivityModelMapper

    @Mock
    lateinit var mockedExportActivityToStravaFileUsecase: ExportTrackingActivityToStravaFileUsecase

    @Mock
    lateinit var mockedClearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase

    @Mock
    lateinit var mockedUploadActivityUsecase: UploadActivityUsecase

    @Mock
    lateinit var mockedRouteTrackingState: RouteTrackingState

    @Mock
    lateinit var mockedGetTrackedLocationsUsecase: GetTrackedLocationsUsecase

    @Mock
    lateinit var mockedSaveExportActivityLocationsUsecase: SaveExportActivityLocationsUsecase

    @Mock
    lateinit var mockedClearExportActivityLocationUsecase: ClearExportActivityLocationUsecase

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
        mockedUploadActivityUsecase,
        mockedClearRouteTrackingStateUsecase,
        mockedSaveExportActivityLocationsUsecase,
        mockedExportActivityToStravaFileUsecase,
        mockedClearExportActivityLocationUsecase,
        mockedActivityMapper,
        mockedExternalAppProvidersRepository,
        mockedAuthenticationState,
        mockedLocationDataSource
    )
}
