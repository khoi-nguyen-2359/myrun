package akio.apps.myrun.data.location.api.model

data class LocationRequestConfig(
    val updateInterval: Long = 0L,
    val fastestUpdateInterval: Long = 0L,
    val smallestDisplacement: Float = 0f,
)
