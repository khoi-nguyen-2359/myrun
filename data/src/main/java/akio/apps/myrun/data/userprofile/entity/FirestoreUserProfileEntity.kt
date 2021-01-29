package akio.apps.myrun.data.userprofile.entity

import com.google.firebase.firestore.PropertyName

// for Firebase object converter
class FirestoreUserProfileEntity(
    @JvmField @PropertyName("uid")
    var uid: String = "",

    @JvmField @PropertyName("displayName")
    var displayName: String? = null,

    @JvmField @PropertyName("photoUrl")
    var photoUrl: String? = null,

    @JvmField @PropertyName("email")
    var email: String? = null,

    @JvmField @PropertyName("phoneNumber")
    var phoneNumber: String? = null,

    @JvmField @PropertyName("gender")
    var gender: String? = null,

    @JvmField @PropertyName("height")
    var height: Float? = null,

    @JvmField @PropertyName("weight")
    var weight: Float? = null
)
