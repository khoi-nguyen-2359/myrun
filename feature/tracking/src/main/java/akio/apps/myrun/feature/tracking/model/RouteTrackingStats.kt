package akio.apps.myrun.feature.tracking.model

internal data class RouteTrackingStats(
    val distance: Double = 0.0,
    val speed: Double = 0.0,
    val duration: Long = 0,
)
