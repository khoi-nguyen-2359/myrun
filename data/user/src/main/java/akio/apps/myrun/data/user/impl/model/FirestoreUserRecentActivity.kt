package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserRecentActivity(
    @PropertyName(FirestoreUserRecentActivityFields.FIELD_PLACE)
    val placeComponents: List<String>? = null,

    @PropertyName(FirestoreUserRecentActivityFields.FIELD_ACTIVE_TIME)
    val activeTime: Long? = null,
)
