package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserProfile(
    @PropertyName("uid")
    val uid: String = "",

    @PropertyName("displayName")
    val displayName: String? = null,

    @PropertyName("photoUrl")
    val photoUrl: String? = null,

    @PropertyName("email")
    val email: String? = null,

    @PropertyName("phoneNumber")
    val phoneNumber: String? = null,

    @PropertyName("gender")
    val gender: String? = null,

    @PropertyName("height")
    val height: Float? = null,

    @PropertyName("weight")
    val weight: Float? = null
)
