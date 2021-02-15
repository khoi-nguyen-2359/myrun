package akio.apps.myrun.data.activity.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreActivityAthleteInfo(
    @JvmField @PropertyName("userId")
    val userId: String = "",
    @JvmField @PropertyName("userName")
    val userName: String? = null,
    @JvmField @PropertyName("userAvatar")
    val userAvatar: String? = null
)
