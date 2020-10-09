package akio.apps.myrun.data.activity

data class ActivityDataEntity(
    override val id: String,
    override val userId: String,
    override val activityType: ActivityType,

    // info
    override val name: String,
    override val routeImage: String,

    // stats
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long,
    override val distance: Double,
    override val encodedPolyline: String
) : ActivityEntity