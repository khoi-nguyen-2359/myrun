package akio.apps.myrun.data.user.api

/**
 * Measurement unit converter class which helps convert tracking values from data storage unit to any other
 * unit.
 */
sealed class UnitConverter<R, T : Number> {
    /**
     * Raw value is the value in data storage.
     */
    abstract fun fromRawValue(rawValue: R): T

    object DistanceMile : UnitConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue / 1609.34
    }

    object DistanceKm : UnitConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue / 1000
    }

    object TimeMinute : UnitConverter<Long, Double>() {
        override fun fromRawValue(rawValue: Long): Double = rawValue / 60000.0
    }

    object TimeHour : UnitConverter<Long, Double>() {
        override fun fromRawValue(rawValue: Long): Double = rawValue / 3600000.0
    }

    object TimeSecond : UnitConverter<Long, Double>() {
        override fun fromRawValue(rawValue: Long): Double = rawValue / 1000.0
    }

    object SpeedKmPerHour : UnitConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue * 3.6
    }

    object SpeedMilePerHour : UnitConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue * 0.621371
    }

    object SpeedKmPerMinute : UnitConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue * 0.06
    }

    object PaceMinutePerMile : UnitConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue / 0.621371
    }

    object DoubleRaw : UnitConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue
    }

    object FloatRaw : UnitConverter<Float, Double>() {
        override fun fromRawValue(rawValue: Float): Double = rawValue * 1.0
    }

    object WeightPound : UnitConverter<Float, Double>() {
        override fun fromRawValue(rawValue: Float): Double = rawValue * 2.20462
    }
}
