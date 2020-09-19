package akio.apps.myrun.data.routetracking.model

data class TrackingLocationEntity(
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)