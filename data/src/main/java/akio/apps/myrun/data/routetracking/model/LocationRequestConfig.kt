package akio.apps.myrun.data.routetracking.model

data class LocationRequestConfig(
    val updateInterval: Long,
    val fastestUpdateInterval: Long,
    val smallestDisplacement: Float
) {
    companion object {
        fun foo(): LocationRequestConfig = LocationRequestConfig(0L, 0L, 0f)
    }
}
