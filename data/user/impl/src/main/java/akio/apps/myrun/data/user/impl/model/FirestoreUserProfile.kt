package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserProfile(
    @PropertyName(FirestoreUserProfileFields.FIELD_UID)
    val uid: String = "",

    @PropertyName(FirestoreUserProfileFields.FIELD_DISPLAY_NAME)
    val displayName: String = "",

    @PropertyName(FirestoreUserProfileFields.FIELD_GENDER_ID)
    val genderId: Int = FirestoreUserGender.Others.genderId,

    @PropertyName(FirestoreUserProfileFields.FIELD_WEIGHT)
    val weight: Float = 0f,

    @PropertyName(FirestoreUserProfileFields.FIELD_BIRTHDATE)
    val birthdate: Long = 0,
)
