package akio.apps.myrun.data.user.api

/**
 * Measurement unit converter class which helps convert tracking values from data storage unit to any other
 * unit.
 */
sealed class TrackValueConverter<R, T : Number> {
    /**
     * Raw value is the value in data storage.
     */
    abstract fun fromRawValue(rawValue: R): T

    object DistanceMile : TrackValueConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue / 1609.34
    }

    object DistanceKm : TrackValueConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue / 1000
    }

    object TimeMinute : TrackValueConverter<Long, Double>() {
        override fun fromRawValue(rawValue: Long): Double = rawValue / 60000.0
    }

    object TimeHour : TrackValueConverter<Long, Double>() {
        override fun fromRawValue(rawValue: Long): Double = rawValue / 3600000.0
    }

    object TimeSecond : TrackValueConverter<Long, Double>() {
        override fun fromRawValue(rawValue: Long): Double = rawValue / 1000.0
    }

    object SpeedKmPerHour : TrackValueConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue * 3.6
    }

    object SpeedMilePerHour : TrackValueConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue * 0.621371
    }

    object SpeedKmPerMinute : TrackValueConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue * 0.06
    }

    object PaceMinutePerMile : TrackValueConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue / 0.621371
    }

    object DoubleRaw : TrackValueConverter<Double, Double>() {
        override fun fromRawValue(rawValue: Double): Double = rawValue
    }
}
