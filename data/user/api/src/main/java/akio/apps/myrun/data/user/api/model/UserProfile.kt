package akio.apps.myrun.data.user.api.model

data class UserProfile(
    val accountId: String,
    val name: String = "",
    val email: String? = null,
    val phone: String?,
    val gender: Gender = Gender.Others,
    val weight: Float = 0f,
    val photo: String?,
    val birthdate: Long = 0,
)
