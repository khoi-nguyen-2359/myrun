package akio.apps.myrun.data.activity.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreActivity(
    @JvmField @PropertyName("id")
    val id: String = "",

    // activity info
    @JvmField @PropertyName("activityType")
    val activityType: FirestoreActivityType = FirestoreActivityType.Unknown,
    @JvmField @PropertyName("name")
    val name: String = "",
    @JvmField @PropertyName("routeImage")
    val routeImage: String = "",
    @JvmField @PropertyName("placeIdentifier")
    val placeIdentifier: String? = null,

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
    val cyclingData: FirestoreCyclingData? = null,

    // user info
    @JvmField @PropertyName("athleteInfo")
    val athleteInfo: FirestoreActivityAthleteInfo = FirestoreActivityAthleteInfo()
)
