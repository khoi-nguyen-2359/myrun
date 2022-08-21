package akio.apps.myrun.data.user.api.model

data class UserFollowPagingToken(
    val userId: String,
    val status: FollowStatus,
    val userName: String
)
