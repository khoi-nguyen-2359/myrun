package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserRecentActivity(
    @PropertyName("place")
    val place: String? = null,

    @PropertyName("activeTime")
    val activeTime: Long? = null,
)
