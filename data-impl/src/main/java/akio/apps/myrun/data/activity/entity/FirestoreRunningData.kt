package akio.apps.myrun.data.activity.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreRunningData(
    // stats
    @JvmField @PropertyName("pace")
    val pace: Double = 0.0,
    @JvmField @PropertyName("cadence")
    val cadence: Int = 0
)
