package akio.apps.myrun.data.activity

interface ActivityEntity {
    val id: String
    val activityType: ActivityType

    // stats
    val startTime: Long
    val endTime: Long
    val duration: Long
}