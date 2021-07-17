package akio.apps.myrun.data.recentplace.entity

import com.google.firebase.firestore.PropertyName

data class FirestorePlaceIdentifier(
    @PropertyName("placeIdentifier")
    val placeIdentifier: String? = null
)
