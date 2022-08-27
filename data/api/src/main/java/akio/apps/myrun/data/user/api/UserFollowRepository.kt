package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowCounter
import akio.apps.myrun.data.user.api.model.UserFollowPagingToken
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.data.user.api.model.UserFollowType
import kotlinx.coroutines.flow.Flow

interface UserFollowRepository {
    suspend fun getUserFollowings(userId: String): List<UserFollow>
    suspend fun getUserFollowers(userId: String): List<UserFollow>

    fun getUserFollowCounterFlow(userId: String): Flow<UserFollowCounter>

    suspend fun followUser(userId: String, followSuggestion: UserFollowSuggestion)
    suspend fun unfollowUser(userId: String, unfollowUserId: String)

    /**
     * Accept a user at [followerId] to follow user at [userId]. If the follow request is not
     * existing (was deleted by follower), this action fails and returns false.
     */
    suspend fun acceptFollower(userId: String, followerId: String)
    suspend fun deleteFollower(userId: String, followerId: String)

    suspend fun getUserFollowByRecentActivity(
        userId: String,
        placeComponent: String,
        limit: Long,
        startAfterActiveTime: Long,
    ): List<UserFollowSuggestion>
    suspend fun getUserFollows(
        userId: String,
        followType: UserFollowType,
        limit: Int,
        pagingToken: UserFollowPagingToken?,
    ): List<UserFollow>
}
