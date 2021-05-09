package akio.apps.myrun.data.activityfile.model

data class ActivityLocation(
    val activityId: String,
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)
