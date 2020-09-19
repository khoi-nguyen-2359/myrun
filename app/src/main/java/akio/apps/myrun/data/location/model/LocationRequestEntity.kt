package akio.apps.myrun.data.location.model

data class LocationRequestEntity(
    val fastestInterval: Long,
    val interval: Long,
    val maxWaitTime: Long,
    val priority: Int,
    val smallestDisplacement: Float
)