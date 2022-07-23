package akio.apps.myrun.data.activity.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreActivityAthleteInfo(
    @PropertyName("userId")
    val userId: String = "",
    @PropertyName("userName")
    val userName: String? = null,
    @PropertyName("userAvatar")
    val userAvatar: String? = null,
)
