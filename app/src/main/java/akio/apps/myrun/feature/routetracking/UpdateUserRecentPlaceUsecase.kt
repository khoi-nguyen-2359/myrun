package akio.apps.myrun.feature.routetracking

interface UpdateUserRecentPlaceUsecase {
    suspend fun updateUserRecentPlace(lat: Double, lng: Double)
}