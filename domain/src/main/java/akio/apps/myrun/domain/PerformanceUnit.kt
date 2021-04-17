package akio.apps.myrun.domain

sealed class PerformanceUnit<T>(val id: String) {
    abstract fun fromRawValue(rawValue: Double): T

    object DistanceMile : PerformanceUnit<Double>("DistanceMile") {
        override fun fromRawValue(rawValue: Double): Double = (rawValue / 1000) / 1.609
    }

    object DistanceKm : PerformanceUnit<Double>("DistanceKm") {
        override fun fromRawValue(rawValue: Double): Double = rawValue / 1000
    }

    object PaceMinutePerKm : PerformanceUnit<Double>("PaceMinutePerKm") {
        override fun fromRawValue(rawValue: Double): Double = rawValue
    }

    object SpeedKmPerHour : PerformanceUnit<Double>("SpeedKmPerHour") {
        override fun fromRawValue(rawValue: Double): Double = rawValue
    }

    object TimeMillisecond : PerformanceUnit<Long>("TimeMillisecond") {
        override fun fromRawValue(rawValue: Double): Long = rawValue.toLong()
    }
}
