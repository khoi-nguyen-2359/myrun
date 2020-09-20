package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.location.model.LatLng
import akio.apps.myrun.feature._base.utils.toLatLng
import akio.apps.myrun.feature.routetracking.GetMapInitialLocationUsecase
import javax.inject.Inject

class GetMapInitialLocationUsecaseImpl @Inject constructor(
    private val locationDataSource: LocationDataSource
) : GetMapInitialLocationUsecase {
    override suspend fun getMapInitialLocation(): LatLng {
        return locationDataSource.getLastLocation()?.toLatLng()
            ?: LatLng(10.8231, 106.6297)    // saigon
    }
}