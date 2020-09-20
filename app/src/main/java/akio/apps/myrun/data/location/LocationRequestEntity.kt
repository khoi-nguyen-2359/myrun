package akio.apps.myrun.data.location

data class LocationRequestEntity(
    val fastestInterval: Long,
    val interval: Long,
    val maxWaitTime: Long,
    val priority: Int,
    val smallestDisplacement: Float
)