package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestorePlaceIdentifier(
    @PropertyName("placeIdentifier")
    val placeIdentifier: String? = null,
)
