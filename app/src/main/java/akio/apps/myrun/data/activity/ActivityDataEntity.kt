package akio.apps.myrun.data.activity

data class ActivityDataEntity(
    override val id: String,
    override val activityType: ActivityType,
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long
) : ActivityEntity