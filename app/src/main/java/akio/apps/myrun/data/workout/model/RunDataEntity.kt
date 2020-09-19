package akio.apps.myrun.data.workout.model

data class RunDataEntity(
    // info
    val routePhoto: String = "",

    // stats
    val avgPace: Double = 0.0,
    val distance: Double = 0.0,

    // data points
    val locations: String? = null
)