package akio.apps.myrun.feature.core.measurement

data class TrackUnitFormatterSet(
    val distanceFormatter: TrackUnitFormatter.DistanceUnitFormatter,
    val paceFormatter: TrackUnitFormatter.PaceUnitFormatter,
    val speedFormatter: TrackUnitFormatter.SpeedUnitFormatter,
    val durationFormatter: TrackUnitFormatter.DurationUnitFormatter,
)
