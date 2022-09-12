package akio.apps.myrun.data.user.api.model

data class UserFollowSuggestion(
    val uid: String,
    val displayName: String,
    val photoUrl: String?,
    val lastActiveTime: Long,
)
