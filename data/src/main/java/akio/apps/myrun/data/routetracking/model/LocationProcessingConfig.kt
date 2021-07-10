package akio.apps.myrun.data.routetracking.model

data class LocationProcessingConfig(
    val isAvgAccumulatorEnabled: Boolean = false,
    val isSpeedFilterEnabled: Boolean = false
)
