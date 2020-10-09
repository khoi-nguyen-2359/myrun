package akio.apps.myrun.data.recentplace.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreRecentPlace(
    @JvmField @PropertyName("areaIdentifier")
    val areaIdentifier: String
)