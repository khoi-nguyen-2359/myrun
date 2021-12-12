package akio.apps.myrun.domain.activity.api.model

data class ActivityLocation(
    /**
     * Elapsed time since activity started.
     */
    val elapsedTime: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Double
)
