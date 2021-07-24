package akio.apps.myrun.data.userprofile.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreUserEntity(
    @PropertyName("profile")
    val profile: FirestoreUserProfileEntity = FirestoreUserProfileEntity()
)
