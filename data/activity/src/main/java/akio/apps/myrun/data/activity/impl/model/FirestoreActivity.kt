package akio.apps.myrun.data.activity.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreActivity(
    @PropertyName("id")
    val id: String = "",

    // activity info
    @PropertyName("activityType")
    val activityType: Int = FirestoreActivityType.Unknown.id,
    @PropertyName("name")
    val name: String = "",
    @PropertyName("routeImage")
    val routeImage: String = "",
    @PropertyName("placeIdentifier")
    val placeIdentifier: String? = null,
    @PropertyName("placeComponents")
    val placeComponents: List<String>? = null,

    // stats
    @PropertyName("startTime")
    val startTime: Long = 0,
    @PropertyName("endTime")
    val endTime: Long = 0,
    @PropertyName("duration")
    val duration: Long = 0,
    @PropertyName("distance")
    val distance: Double = 0.0,
    @PropertyName("encodedPolyline")
    val encodedPolyline: String = "",

    // activity data
    @PropertyName("runningData")
    val runningData: FirestoreRunningData? = null,
    @PropertyName("cyclingData")
    val cyclingData: FirestoreCyclingData? = null,

    // user info
    @PropertyName("athleteInfo")
    val athleteInfo: FirestoreActivityAthleteInfo = FirestoreActivityAthleteInfo(),
)
