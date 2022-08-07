package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.user.api.model.UserFollow
import kotlinx.coroutines.flow.Flow

interface UserFollowRepository {
    fun getUserFollowingsFlow(userId: String): Flow<List<UserFollow>>
    fun getUserFollowers(userId: String): List<String>
    fun followUser(userId: String, followUserId: String)
    fun unfollowUser(userId: String, unfollowUserId: String)
}
