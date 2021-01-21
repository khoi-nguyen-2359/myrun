package akio.apps.myrun.data.userprofile.entity

import com.google.firebase.firestore.PropertyName

class FirestoreUserEntity {
    @JvmField
    @PropertyName("profile")
    var profile: FirestoreUserProfileEntity? = null
}
