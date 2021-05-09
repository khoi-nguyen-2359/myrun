package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.routetracking.model.LatLng
import akio.apps.test.wheneverBlocking
import android.location.Location
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class GetMapInitialLocationUsecaseTest {

    @Mock
    lateinit var mockedLocationDataSource: LocationDataSource

    lateinit var testee: GetMapInitialLocationUsecase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testee = GetMapInitialLocationUsecase(mockedLocationDataSource)
    }

    @Test
    fun testGetMapInitialLocationSuccess() {
        val mockedLocation = mock<Location> {
            on { latitude }.thenReturn(10.0)
            on { longitude }.thenReturn(20.0)
        }
        val mockedLatLng = LatLng(10.0, 20.0)

        wheneverBlocking(mockedLocationDataSource) { getLastLocation() }.thenReturn(mockedLocation)

        runBlockingTest {
            assertEquals(
                mockedLatLng,
                testee.getMapInitialLocation()
            )
        }

        verifyBlocking(mockedLocationDataSource) { getLastLocation() }
    }

    @Test
    fun testGetInitialLocationWhenLastKnownLocationIsNull() {
        val mockedLatLng = LatLng(10.8231, 106.6297)

        wheneverBlocking(mockedLocationDataSource) { getLastLocation() }.thenReturn(null)

        runBlockingTest {
            assertEquals(
                mockedLatLng,
                testee.getMapInitialLocation()
            )
        }

        verifyBlocking(mockedLocationDataSource) { getLastLocation() }
    }
}
