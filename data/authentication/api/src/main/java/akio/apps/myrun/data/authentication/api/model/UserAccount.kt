package akio.apps.myrun.data.authentication.api.model

data class UserAccount(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val phoneNumber: String?
)
