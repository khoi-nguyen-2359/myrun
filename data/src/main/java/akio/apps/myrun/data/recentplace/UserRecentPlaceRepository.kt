package akio.apps.myrun.data.recentplace

typealias PlaceIdentifier = String

interface UserRecentPlaceRepository {
    suspend fun saveRecentPlace(userId: String, areaIdentifier: PlaceIdentifier)
    suspend fun getRecentPlaceIdentifier(userId: String): PlaceIdentifier?
}
