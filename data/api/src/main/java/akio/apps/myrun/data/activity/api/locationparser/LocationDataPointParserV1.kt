package akio.apps.myrun.data.activity.api.locationparser

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import java.util.concurrent.TimeUnit

internal class LocationDataPointParserV1 : LocationDataPointParser {
    override fun flatten(dataPoint: List<ActivityLocation>): List<Double> =
        dataPoint.flatMap {
            listOf(it.elapsedTime.toDouble(), it.latitude, it.longitude, it.altitude)
        }

    override fun build(firestoreDataPoints: List<Double>): List<ActivityLocation> {
        val firstLocationTime = firestoreDataPoints.firstOrNull()?.toLong() ?: 0L
        val fixFunction = createActivityLocationTimeFixFunction(firstLocationTime)
        return firestoreDataPoints.chunked(4) {
            val fixedLocationTime = fixFunction(it[0].toLong())
            ActivityLocation(
                elapsedTime = fixedLocationTime,
                latitude = it[1],
                longitude = it[2],
                altitude = it[3],
                speed = 0.0
            )
        }
    }

    /**
     * 1.5.0 Migration:
     * - Convert activities' location time (calendar time, maybe in second) -> activity time
     * (activity elapsed time in millisecond)
     * TODO: Remove this after migration are done on Firestore.
     */
    private fun createActivityLocationTimeFixFunction(firstLocationTime: Long): (Long) -> Long {
        // won't fix the whole array if the first location's time is already elapsed time.
        if (firstLocationTime < ACTIVITY_LOCATION_TIME_FIX_MIN_THRESHOLD) {
            return { time -> time } // no fixes needed
        }

        val shouldFixLocationTimeUnit =
            firstLocationTime < ACTIVITY_LOCATION_TIME_UNIT_FIX_MAX_THRESHOLD
        return { oldLocationTime ->
            // convert from calendar time to elapsed time
            var fixedLocationTime = oldLocationTime - firstLocationTime
            if (shouldFixLocationTimeUnit) {
                // convert from second to millisecond
                fixedLocationTime *= 1000
            }
            fixedLocationTime
        }
    }

    companion object {
        // ten days in milliseconds
        private val ACTIVITY_LOCATION_TIME_FIX_MIN_THRESHOLD: Long = TimeUnit.DAYS.toMillis(10)

        private const val ACTIVITY_LOCATION_TIME_UNIT_FIX_MAX_THRESHOLD: Long =
            2_000_000_000 // 2 bil seconds
    }
}
