package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserProfileUpdateMap(
    @PropertyName("profile")
    val profile: MutableMap<String, Any> = mutableMapOf(),
) {
    fun displayName(value: String) {
        profile[FirestoreUserProfileFields.FIELD_DISPLAY_NAME] = value
    }

    fun photoUrl(value: String) {
        profile[FirestoreUserProfileFields.FIELD_PHOTO_URL] = value
    }

    fun gender(value: Int) {
        profile[FirestoreUserProfileFields.FIELD_GENDER_ID] = value
    }

    fun birthdate(value: Long) {
        profile[FirestoreUserProfileFields.FIELD_BIRTHDATE] = value
    }

    fun weight(value: Float) {
        profile[FirestoreUserProfileFields.FIELD_WEIGHT] = value
    }

    fun uid(value: String) {
        profile[FirestoreUserProfileFields.FIELD_UID] = value
    }
}
