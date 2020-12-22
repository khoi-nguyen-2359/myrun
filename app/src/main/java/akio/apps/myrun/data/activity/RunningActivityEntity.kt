package akio.apps.myrun.data.activity

data class RunningActivityEntity(
    val activityData: ActivityEntity,

    // stats
    val pace: Double,
): ActivityEntity by activityData