package akio.apps.myrun.feature.recentplace

interface UpdateUserRecentPlaceUsecase {
    suspend fun updateUserRecentPlace(lat: Double, lng: Double)
}
