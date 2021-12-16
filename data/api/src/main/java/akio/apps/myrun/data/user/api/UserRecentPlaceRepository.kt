package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.common.Resource
import kotlinx.coroutines.flow.Flow

typealias PlaceIdentifier = String

interface UserRecentPlaceRepository {
    suspend fun saveRecentPlace(userId: String, areaIdentifier: PlaceIdentifier)
    suspend fun getRecentPlaceIdentifier(userId: String): PlaceIdentifier?
    fun getRecentPlaceIdentifierFlow(userId: String): Flow<Resource<out PlaceIdentifier?>>
}
