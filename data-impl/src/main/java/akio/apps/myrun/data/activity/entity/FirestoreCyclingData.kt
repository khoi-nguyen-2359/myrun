package akio.apps.myrun.data.activity.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreCyclingData(
    // stats
    @PropertyName("speed")
    val speed: Double = 0.0,
)
