package akio.apps.myrun.data.recentplace

import akio.apps.myrun.data.recentplace.entity.PlaceIdentifier

interface UserRecentPlaceRepository {
    suspend fun saveRecentPlace(userId: String, areaIdentifier: String)
    suspend fun getRecentPlaceIdentifier(userId: String): PlaceIdentifier?
}
