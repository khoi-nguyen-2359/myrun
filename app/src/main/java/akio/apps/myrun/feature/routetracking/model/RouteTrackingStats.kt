package akio.apps.myrun.feature.routetracking.model

import akio.apps.myrun.data.activity.ActivityType

data class RouteTrackingStats(
    val distance: Double,
    val speed: Double,
    val duration: Long
)