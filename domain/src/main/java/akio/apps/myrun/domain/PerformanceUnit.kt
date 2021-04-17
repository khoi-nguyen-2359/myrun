package akio.apps.myrun.domain

import kotlin.reflect.KClass

abstract class PerformanceUnit<T : Number>(val id: String) {
    abstract fun fromRawValue(rawValue: Number): T

    abstract class RawValueUnit<T : Number>(id: String, private val outputClass: KClass<T>) :
        PerformanceUnit<T>(id) {
        @Suppress("UNCHECKED_CAST")
        override fun fromRawValue(rawValue: Number): T = when (outputClass) {
            Double::class -> rawValue.toDouble()
            Long::class -> rawValue.toLong()
            Int::class -> rawValue.toInt()
            else -> throw Exception("Unit type not supported")
        } as T
    }

    object DistanceMile : PerformanceUnit<Double>("DistanceMile") {
        override fun fromRawValue(rawValue: Number): Double = (rawValue.toDouble() / 1000) / 1.609
    }

    object DistanceKm : PerformanceUnit<Double>("DistanceKm") {
        override fun fromRawValue(rawValue: Number): Double = rawValue.toDouble() / 1000
    }

    object TimeMinute : PerformanceUnit<Double>("TimeMinute") {
        override fun fromRawValue(rawValue: Number): Double = rawValue.toDouble() / 60000
    }

    object TimeHour : PerformanceUnit<Double>("TimeHour") {
        override fun fromRawValue(rawValue: Number): Double = rawValue.toDouble() / 3600000
    }

    object PaceMinutePerKm : RawValueUnit<Double>("PaceMinutePerKm", Double::class)
    object SpeedKmPerHour : RawValueUnit<Double>("SpeedKmPerHour", Double::class)
    object TimeMillisecond : RawValueUnit<Long>("TimeMillisecond", Long::class)
    object CadenceStepPerMinute : RawValueUnit<Int>("CadenceStepPerMinute", Int::class)
}
