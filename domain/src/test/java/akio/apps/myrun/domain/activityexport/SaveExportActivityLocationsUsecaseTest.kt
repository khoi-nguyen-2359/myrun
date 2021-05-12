package akio.apps.myrun.domain.activityexport

import akio.apps.myrun.data.activityexport.ExportActivityLocationRepository
import akio.apps.myrun.data.activityexport.model.ActivityLocation
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest

@ExperimentalCoroutinesApi
class SaveExportActivityLocationsUsecaseTest {
    private lateinit var usecase: SaveExportActivityLocationsUsecase
    private lateinit var mockedExportActivityLocationRepository: ExportActivityLocationRepository
    private lateinit var mockedRouteTrackingLocationRepository: RouteTrackingLocationRepository

    @Before
    fun setup() {
        mockedRouteTrackingLocationRepository = mock()
        mockedExportActivityLocationRepository = mock()
        usecase = SaveExportActivityLocationsUsecase(
            mockedRouteTrackingLocationRepository,
            mockedExportActivityLocationRepository
        )
    }

    @Test
    fun testSaveExportActivityLocations() = runBlockingTest {
        val activityId = "activityId"
        val trackingLocationList = listOf(
            TrackingLocationEntity(time = 0L, latitude = 1.0, longitude = 2.0, altitude = 3.0),
            TrackingLocationEntity(time = 1L, latitude = 2.0, longitude = 3.0, altitude = 4.0)
        )
        whenever(mockedRouteTrackingLocationRepository.getAllLocations())
            .thenReturn(trackingLocationList)
        usecase(activityId)
        val activityLocationList = listOf(
            ActivityLocation(
                activityId,
                time = 0L,
                latitude = 1.0,
                longitude = 2.0,
                altitude = 3.0
            ),
            ActivityLocation(activityId, time = 1L, latitude = 2.0, longitude = 3.0, altitude = 4.0)
        )
        verify(mockedExportActivityLocationRepository).saveActivityLocations(activityLocationList)
    }
}
