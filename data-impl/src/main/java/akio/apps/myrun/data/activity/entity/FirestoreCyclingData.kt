package akio.apps.myrun.data.activity.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreCyclingData(
    // stats
    @JvmField @PropertyName("speed")
    val speed: Double = 0.0,
)
