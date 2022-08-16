package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreCounter(
    @PropertyName("count")
    val count: Int = 0
)
