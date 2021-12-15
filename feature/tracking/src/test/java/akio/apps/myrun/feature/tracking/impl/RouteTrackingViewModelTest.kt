package akio.apps.myrun.feature.tracking.impl

import akio.apps._base.InstantTaskExecutorTest
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus.STOPPED
import akio.apps.myrun.domain.launchcatching.LaunchCatchingDelegateImpl
import akio.apps.myrun.domain.tracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.tracking.StoreTrackingActivityDataUsecase
import akio.apps.myrun.feature.tracking.RouteTrackingViewModel
import akio.apps.test.whenBlocking
import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

@ExperimentalCoroutinesApi
class RouteTrackingViewModelTest : InstantTaskExecutorTest() {

    @Mock
    lateinit var mockedAuthenticationState: UserAuthenticationState

    @Mock
    lateinit var mockedExternalAppProvidersRepository: ExternalAppProvidersRepository

    private lateinit var mockedClearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase
    private lateinit var mockedRouteTrackingLocationRepository: RouteTrackingLocationRepository

    @Mock
    lateinit var mockedRouteTrackingState: RouteTrackingState

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
        mockedClearRouteTrackingStateUsecase = mock()
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
