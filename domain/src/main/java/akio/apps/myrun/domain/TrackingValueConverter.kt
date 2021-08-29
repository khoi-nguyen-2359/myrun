package akio.apps.myrun.domain

/**
 * Measurement unit converter class which helps convert tracking values from data storage unit to any other
 * unit.
 */
sealed class TrackingValueConverter<R, T : Number>(val id: String) {
    /**
     * Raw value is the value in data storage.
     */
    abstract fun fromRawValue(rawValue: R): T

    object DistanceMile : TrackingValueConverter<Double, Double>("DistanceMile") {
        override fun fromRawValue(rawValue: Double): Double = (rawValue / 1000) / 1.609
    }

    object DistanceKm : TrackingValueConverter<Double, Double>("DistanceKm") {
        override fun fromRawValue(rawValue: Double): Double = rawValue / 1000
    }

    object TimeMinute : TrackingValueConverter<Long, Double>("TimeMinute") {
        override fun fromRawValue(rawValue: Long): Double = rawValue / 60000.0
    }

    object TimeHour : TrackingValueConverter<Long, Double>("TimeHour") {
        override fun fromRawValue(rawValue: Long): Double = rawValue / 3600000.0
    }

    object TimeSecond : TrackingValueConverter<Long, Double>("TimeSecond") {
        override fun fromRawValue(rawValue: Long): Double = rawValue / 1000.0
    }

    object SpeedKmPerHour : TrackingValueConverter<Double, Double>("SpeedKmPerHour") {
        override fun fromRawValue(rawValue: Double): Double = rawValue * 3.6
    }

    object SpeedKmPerMinute : TrackingValueConverter<Double, Double>("SpeedKmPerMinute") {
        override fun fromRawValue(rawValue: Double): Double = rawValue * 0.06
    }
}
