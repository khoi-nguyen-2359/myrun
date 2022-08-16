package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowCounter
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion

interface UserFollowRepository {
    suspend fun getUserFollowings(userId: String): List<UserFollow>
    suspend fun getUserFollowers(userId: String): List<UserFollow>
    suspend fun getUserFollowCounter(userId: String): UserFollowCounter
    suspend fun followUser(userId: String, followSuggestion: UserFollowSuggestion)
    fun unfollowUser(userId: String, unfollowUserId: String)
    suspend fun getUserFollowByRecentActivity(
        userId: String,
        placeComponent: String,
        limit: Long,
        startAfterActiveTime: Long,
    ): List<UserFollowSuggestion>
}
