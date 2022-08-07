package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserFollow(
    @PropertyName(FirestoreUserProfileFields.FIELD_DISPLAY_NAME)
    val displayName: String = "",

    @PropertyName(FirestoreUserProfileFields.FIELD_PHOTO_URL)
    val photoUrl: String? = null,
)
