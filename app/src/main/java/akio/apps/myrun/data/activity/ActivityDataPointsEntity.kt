package akio.apps.myrun.data.activity

data class ActivityDataPointsEntity(
    // [timestamp, latitude, longitude, altitude]
    val locations: List<List<Double>>? = null,

    // [timestamp, heartRate]
    val heartRate: List<List<Double>>? = null,

    // [timestamp, cadence]
    val steppingCadence: List<List<Double>>? = null,

    // [timestamp, cadence]
    val pedalingCadence: List<List<Double>>? = null,

    // [timestamp, speed]
    val speed: List<List<Double>>? = null
)