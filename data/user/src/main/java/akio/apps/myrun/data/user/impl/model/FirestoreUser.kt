package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUser(
    @PropertyName("preferences")
    val preferences: FirestoreUserPreferences = FirestoreUserPreferences(),

    @PropertyName("profile")
    val profile: FirestoreUserProfile = FirestoreUserProfile(),

    @PropertyName("recentActivity")
    val recentActivity: FirestoreUserRecentActivity = FirestoreUserRecentActivity(),
)
