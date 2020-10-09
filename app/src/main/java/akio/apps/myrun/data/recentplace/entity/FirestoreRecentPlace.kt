package akio.apps.myrun.data.recentplace.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreRecentPlace(
    @JvmField @PropertyName("addressComponents")
    val addressComponents: List<String>
)