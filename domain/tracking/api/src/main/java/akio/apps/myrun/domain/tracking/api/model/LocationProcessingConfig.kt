package akio.apps.myrun.domain.tracking.api.model

data class LocationProcessingConfig(
    val isAvgAccumulatorEnabled: Boolean = false,
    val isSpeedFilterEnabled: Boolean = false
)
