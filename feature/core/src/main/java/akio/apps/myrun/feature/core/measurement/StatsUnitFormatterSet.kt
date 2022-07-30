package akio.apps.myrun.feature.core.measurement

data class StatsUnitFormatterSet(
    val distanceFormatter: StatsUnitFormatter<Double>,
    val durationFormatter: StatsUnitFormatter<Long>,
)
