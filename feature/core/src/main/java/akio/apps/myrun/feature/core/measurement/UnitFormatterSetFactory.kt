package akio.apps.myrun.feature.core.measurement

import akio.apps.myrun.data.user.api.model.MeasureSystem

object UnitFormatterSetFactory {
    fun createUnitFormatterSet(measureSystem: MeasureSystem): TrackUnitFormatterSet =
        when (measureSystem) {
            MeasureSystem.Metric -> TrackUnitFormatterSet(
                TrackUnitFormatter.DistanceKm,
                TrackUnitFormatter.PaceMinutePerKm,
                TrackUnitFormatter.SpeedKmPerHour,
                TrackUnitFormatter.DurationHourMinuteSecond
            )
            MeasureSystem.Imperial -> TrackUnitFormatterSet(
                TrackUnitFormatter.DistanceMile,
                TrackUnitFormatter.PaceMinutePerMile,
                TrackUnitFormatter.SpeedMilePerHour,
                TrackUnitFormatter.DurationHourMinuteSecond
            )
        }

    fun createBodyWeightUnitFormatter(
        measureSystem: MeasureSystem,
    ): UserProfileUnitFormatter.BodyWeightUnitFormatter =
        when (measureSystem) {
            MeasureSystem.Metric -> UserProfileUnitFormatter.WeightKg
            MeasureSystem.Imperial -> UserProfileUnitFormatter.WeightPound
        }
}
