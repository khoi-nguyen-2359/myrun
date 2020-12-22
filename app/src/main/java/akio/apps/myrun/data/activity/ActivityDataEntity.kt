package akio.apps.myrun.data.activity

data class ActivityDataEntity(
    override val id: String,

    // user info
    override val userId: String,
    override val userName: String?,
    override val userAvatar: String?,

    // info
    override val activityType: ActivityType,
    override val name: String,
    override val routeImage: String,

    // stats
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long,
    override val distance: Double,
    override val encodedPolyline: String
) : ActivityEntity