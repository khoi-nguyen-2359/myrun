package akio.apps.myrun.feature.routetracking.usecase

import akio.apps.myrun._base.utils.toLatLng
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.feature.routetracking.GetMapInitialLocationUsecase
import akio.apps.myrun.feature.routetracking.model.LatLng
import javax.inject.Inject

class GetMapInitialLocationUsecaseImpl @Inject constructor(
    private val locationDataSource: LocationDataSource
) : GetMapInitialLocationUsecase {
    override suspend fun getMapInitialLocation(): LatLng {
        return locationDataSource.getLastLocation()
            ?.toLatLng()
            ?: LatLng(10.8231, 106.6297) // fallback to saigon's location
    }
}
