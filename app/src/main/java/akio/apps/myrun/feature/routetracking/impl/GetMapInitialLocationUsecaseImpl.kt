package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.feature.routetracking.GetMapInitialLocationUsecase
import android.location.Location
import javax.inject.Inject

class GetMapInitialLocationUsecaseImpl @Inject constructor(
    private val locationDataSource: LocationDataSource
) : GetMapInitialLocationUsecase {
    override suspend fun getMapInitialLocation(): Location {
        return locationDataSource.getLastLocation()
    }
}