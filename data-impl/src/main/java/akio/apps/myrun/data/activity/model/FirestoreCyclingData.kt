package akio.apps.myrun.data.activity.model

import com.google.firebase.firestore.PropertyName

data class FirestoreCyclingData(
    // stats
    @PropertyName("speed")
    val speed: Double = 0.0,
)
