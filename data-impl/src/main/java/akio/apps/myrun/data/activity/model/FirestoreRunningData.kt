package akio.apps.myrun.data.activity.model

import com.google.firebase.firestore.PropertyName

data class FirestoreRunningData(
    // stats
    @PropertyName("pace")
    val pace: Double = 0.0,
    @PropertyName("cadence")
    val cadence: Int = 0
)
