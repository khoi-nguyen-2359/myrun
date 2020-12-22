package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.feature.routetracking.model.LatLng

interface GetMapInitialLocationUsecase {
    suspend fun getMapInitialLocation(): LatLng
}