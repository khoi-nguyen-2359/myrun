package akio.apps.myrun.data.userfollow

interface UserFollowRepository {
    fun getUserFollowings(userId: String): List<String>
    fun getUserFollowers(userId: String): List<String>
    fun followUser(userId: String, followUserId: String)
    fun unfollowUser(userId: String, unfollowUserId: String)
}
