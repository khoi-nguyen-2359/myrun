package akio.apps.myrun.data.tracking.api.model

data class LocationProcessingConfig(
    val isAvgAccumulatorEnabled: Boolean = false,
    val isSpeedFilterEnabled: Boolean = false,
)
