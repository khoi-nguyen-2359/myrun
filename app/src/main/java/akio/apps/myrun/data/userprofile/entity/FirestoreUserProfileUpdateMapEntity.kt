package akio.apps.myrun.data.userprofile.entity

class FirestoreUserProfileUpdateMapEntity(
	val profile: MutableMap<String, Any> = mutableMapOf()
) {
	fun displayName(value: String) { profile["displayName"] = value }
	fun photoUrl(value: String) { profile["photoUrl"] = value }
	fun email(value: String) { profile["email"] = value }
	fun phoneNumber(value: String) { profile["phoneNumber"] = value }
	fun gender(value: String) { profile["gender"] = value }
	fun height(value: Float) { profile["height"] = value }
	fun weight(value: Float) { profile["weight"] = value }
	fun uid(value: String) { profile["uid"] = value }
}