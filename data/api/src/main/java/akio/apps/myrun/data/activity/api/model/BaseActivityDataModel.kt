package akio.apps.myrun.data.activity.api.model

/**
 * This interface is for common data fields of an activity.
 * Declare it as internal because it is only used as property delegation for concrete activity
 * data model classes.
 */
internal interface BaseActivityDataModel {
    val id: String

    // user info
    val athleteInfo: AthleteInfo

    // info
    val activityType: ActivityType
    val name: String
    val routeImage: String
    val placeIdentifier: String?

    // stats
    val startTime: Long
    val endTime: Long
    val duration: Long
    val distance: Double

    // data points
    val encodedPolyline: String
}
