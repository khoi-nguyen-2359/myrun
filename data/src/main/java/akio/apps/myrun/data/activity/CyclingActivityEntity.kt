package akio.apps.myrun.data.activity

data class CyclingActivityEntity(
    val activityData: ActivityEntity,

    // stats
    val speed: Double,
) : ActivityEntity by activityData
