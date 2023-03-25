package akio.apps.myrun.data.user.impl.model

import akio.apps.myrun.data.user.impl.model.FirestoreUserPreferences.Companion.DEFAULT_MAP_VISIBILITY_VALUE
import com.google.firebase.firestore.PropertyName

data class FirestoreUserFollow(
    @PropertyName(FirestoreUserProfileFields.FIELD_UID)
    val uid: String = "",

    @PropertyName(FirestoreUserProfileFields.FIELD_DISPLAY_NAME)
    val displayName: String = "",

    @PropertyName(FirestoreUserProfileFields.FIELD_PHOTO_URL)
    val photoUrl: String? = null,

    @PropertyName(FirestoreUserProfileFields.FIELD_FOLLOW_STATUS)
    val status: Int = FirestoreFollowStatus.Requested.rawValue,

    @PropertyName(FirestoreUserProfileFields.FIELD_SHOW_ACTIVITY_MAP_ON_FEED)
    val showActivityMapOnFeed: Boolean = DEFAULT_MAP_VISIBILITY_VALUE,
)
