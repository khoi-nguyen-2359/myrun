package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion

interface UserFollowRepository {
    suspend fun getUserFollowings(userId: String): List<UserFollow>
    fun getUserFollowers(userId: String): List<String>
    suspend fun followUser(userId: String, followSuggestion: UserFollowSuggestion)
    fun unfollowUser(userId: String, unfollowUserId: String)
    suspend fun getUserFollowByRecentActivity(
        userId: String,
        placeComponent: String,
        limit: Long,
        startAfterActiveTime: Long,
    ): List<UserFollowSuggestion>
}
