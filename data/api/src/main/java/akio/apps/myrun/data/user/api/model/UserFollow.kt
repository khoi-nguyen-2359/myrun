package akio.apps.myrun.data.user.api.model

data class UserFollow(
    val uid: String,
    val displayName: String,
    val photoUrl: String?,
    val status: FollowStatus,
    val isMapVisible: Boolean,
)

enum class FollowStatus {
    Requested, Accepted
}
