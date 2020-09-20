package akio.apps.myrun.data.routetracking

data class TrackingLocationEntity(
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)