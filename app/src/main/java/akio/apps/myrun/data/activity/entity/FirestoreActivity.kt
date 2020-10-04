package akio.apps.myrun.data.activity.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreActivity(
    @JvmField @PropertyName("id")
    val id: String = "",
    @JvmField @PropertyName("userId")
    val userId: String = "",
    @JvmField @PropertyName("activityType")
    val activityType: FirestoreActivityType = FirestoreActivityType.Unknown,

    // info
    @JvmField @PropertyName("name")
    val name: String = "",
    @JvmField @PropertyName("routeImage")
    val routeImage: String = "",

    // stats
    @JvmField @PropertyName("startTime")
    val startTime: Long = 0,
    @JvmField @PropertyName("endTime")
    val endTime: Long = 0,
    @JvmField @PropertyName("duration")
    val duration: Long = 0,
    @JvmField @PropertyName("distance")
    val distance: Double = 0.0,
    @JvmField @PropertyName("encodedPolyline")
    val encodedPolyline: String = "",

    // activity data
    @JvmField @PropertyName("runningData")
    val runningData: FirestoreRunningData? = null,
    @JvmField @PropertyName("cyclingData")
    val cyclingData: FirestoreCyclingData? = null
)