package akio.apps.myrun.data.routetracking.dto

data class TrackingLocationEntity(
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)