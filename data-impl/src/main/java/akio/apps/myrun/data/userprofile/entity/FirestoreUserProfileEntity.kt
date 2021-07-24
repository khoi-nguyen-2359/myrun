package akio.apps.myrun.data.userprofile.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreUserProfileEntity(
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
