package akio.apps.myrun.data.activity.model

data class ActivityDataModel(
    override val id: String,

    // info
    override val activityType: ActivityType,
    override val name: String,
    override val routeImage: String,
    override val placeIdentifier: String?,

    // stats
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long,
    override val distance: Double,
    override val encodedPolyline: String,

    // user info
    override val athleteInfo: ActivityModel.AthleteInfo
) : ActivityModel
