package akio.apps.myrun.data.user.api.model

data class UserFollow(
    val uid: String,
    val displayName: String,
    val photoUrl: String?,
    val status: FollowStatus,
)

enum class FollowStatus {
    Requested, Accepted
}
