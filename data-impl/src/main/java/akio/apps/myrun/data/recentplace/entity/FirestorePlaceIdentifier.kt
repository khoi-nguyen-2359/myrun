package akio.apps.myrun.data.recentplace.entity

import com.google.firebase.firestore.PropertyName

data class FirestorePlaceIdentifier(
    @JvmField @PropertyName("placeIdentifier")
    val placeIdentifier: String? = null
)
