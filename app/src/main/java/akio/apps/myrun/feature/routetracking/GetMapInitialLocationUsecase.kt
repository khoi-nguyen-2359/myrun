package akio.apps.myrun.feature.routetracking

interface GetMapInitialLocationUsecase {
    suspend fun getMapInitialLocation(): LatLng
}