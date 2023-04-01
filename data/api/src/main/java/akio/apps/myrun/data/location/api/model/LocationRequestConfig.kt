package akio.apps.myrun.data.location.api.model

data class LocationRequestConfig(
    val updateInterval: Long = 0L,
    val minUpdateInterval: Long = 0L,
    val minUpdateDistance: Float = 0f,
    val maxUpdateInterval: Long = 0L,
)
