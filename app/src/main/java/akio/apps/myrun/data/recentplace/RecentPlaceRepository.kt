package akio.apps.myrun.data.recentplace

interface RecentPlaceRepository {
    suspend fun saveRecentPlace(userId: String, addressComponents: List<String>)
    fun getNearBy(userId: String)
}