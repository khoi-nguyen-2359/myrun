package akio.apps.myrun.feature.core.measurement

data class TrackValueFormatPreference(
    val distanceFormatter: TrackValueFormatter<Double>,
    val paceFormatter: TrackValueFormatter<Double>,
    val speedFormatter: TrackValueFormatter<Double>,
    val durationFormatter: TrackValueFormatter<Long>,
)
