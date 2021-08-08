package akio.apps.myrun.data.userprofile.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUser(
    @PropertyName("profile")
    val profile: FirestoreUserProfile = FirestoreUserProfile()
)
