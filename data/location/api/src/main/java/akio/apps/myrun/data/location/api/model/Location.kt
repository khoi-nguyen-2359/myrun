package akio.apps.myrun.data.location.api.model

data class Location(
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double
)
