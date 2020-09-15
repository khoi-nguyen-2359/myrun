package akio.apps.myrun.data.workout.dto

data class RunDataEntity(
    // info
    val routePhoto: String = "",

    // stats
    val avgPace: Double = 0.0,
    val distance: Double = 0.0,
    val duration: Long = 0,

    // data points
    val locations: String? = null,
    val speeds: List<Float>? = null,
    val steps: List<Int>? = null,
    val cadences: List<Float>? = null
)