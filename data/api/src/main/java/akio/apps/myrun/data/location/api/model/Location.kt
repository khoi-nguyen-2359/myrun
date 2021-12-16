package akio.apps.myrun.data.location.api.model

data class Location(
    /**
     * Elapsed time since system boot.
     */
    val elapsedTime: Long,

    /**
     * Calendar time at the time this location is created.
     */
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double
)
