package akio.apps.myrun.domain.routetracking

import akio.apps.myrun._base.utils.toLatLng
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.data.routetracking.model.LatLng
import javax.inject.Inject

class GetMapInitialLocationUsecase @Inject constructor(
    private val locationDataSource: LocationDataSource
) {
    suspend fun getMapInitialLocation(): LatLng {
        return locationDataSource.getLastLocation()
            ?.toLatLng()
            ?: LatLng(10.8231, 106.6297) // TODO: fallback to saigon's location?
    }
}
