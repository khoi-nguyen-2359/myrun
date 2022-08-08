package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import akio.apps.myrun.data.user.api.model.UserFollow
import kotlinx.coroutines.flow.Flow

interface UserRecentActivityRepository {
    suspend fun saveRecentPlace(userId: String, areaIdentifier: PlaceIdentifier)
    suspend fun getRecentPlaceIdentifier(userId: String): PlaceIdentifier?
    fun getRecentPlaceIdentifierFlow(userId: String): Flow<Resource<out PlaceIdentifier?>>
    suspend fun getUserFollowByRecentActivity(
        userId: String,
        placeComponent: String,
        limit: Long,
    ): List<UserFollow>
}
