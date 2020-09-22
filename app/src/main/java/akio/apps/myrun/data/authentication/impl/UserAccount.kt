package akio.apps.myrun.data.authentication.impl

data class UserAccount(
	val uid: String,
	val email: String?,
	val phoneNumber: String?
)