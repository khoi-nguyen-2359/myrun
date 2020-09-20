package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.data.location.model.LatLng

interface GetMapInitialLocationUsecase {
    suspend fun getMapInitialLocation(): LatLng
}