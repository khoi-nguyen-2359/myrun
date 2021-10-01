package akio.apps.myrun.data.activity.impl.model

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.DataPoint
import com.google.firebase.firestore.PropertyName
import java.util.concurrent.TimeUnit

data class FirestoreDataPointList(
    @PropertyName("data")
    val data: List<Double> = emptyList(),
)

class FirestoreDataPointSerializer<T>(
    private val firestoreDataPointParser: FirestoreDataPointParser<T>,
) {
    fun serialize(dataPoints: List<DataPoint<T>>): FirestoreDataPointList {
        return FirestoreDataPointList(dataPoints.flatMap { firestoreDataPointParser.flatten(it) })
    }
}

class FirestoreDataPointDeserializer<T>(
    private val dataPointParser: FirestoreDataPointParser<T>,
) {
    fun deserialize(firestoreData: FirestoreDataPointList): List<DataPoint<T>> {
        val dataPointList = firestoreData.data
        val dataPointIterator = dataPointList.iterator()
        val result = mutableListOf<DataPoint<T>>()
        while (dataPointIterator.hasNext()) {
            val time = dataPointIterator.next().toLong()
            val value = dataPointParser.build(dataPointIterator)
            result.add(DataPoint(time, value))
        }

        return result
    }
}

interface FirestoreDataPointParser<T> {
    fun flatten(dataPoint: DataPoint<T>): List<Double>
    fun build(firestoreDataIterator: Iterator<Double>): T
}

class FirestoreFloatDataPointParser : FirestoreDataPointParser<Float> {
    override fun flatten(dataPoint: DataPoint<Float>): List<Double> =
        listOf(dataPoint.timestamp.toDouble(), dataPoint.value.toDouble())

    override fun build(firestoreDataIterator: Iterator<Double>): Float =
        firestoreDataIterator.next()
            .toFloat()
}

class FirestoreIntegerDataPointParser : FirestoreDataPointParser<Int> {
    override fun flatten(dataPoint: DataPoint<Int>): List<Double> =
        listOf(dataPoint.timestamp.toDouble(), dataPoint.value.toDouble())

    override fun build(firestoreDataIterator: Iterator<Double>): Int = firestoreDataIterator.next()
        .toInt()
}

class FirestoreLocationDataPointParser {
    fun flatten(dataPoint: List<ActivityLocation>): List<Double> =
        dataPoint.fold(mutableListOf()) { accum, item ->
            accum.add(item.elapsedTime.toDouble())
            accum.add(item.latitude)
            accum.add(item.longitude)
            accum.add(item.altitude)
            accum
        }

    fun build(firestoreDataPoints: List<Double>): List<ActivityLocation> {
        val firstLocationTime = firestoreDataPoints.firstOrNull()?.toLong() ?: 0L
        val fixFunction = createActivityLocationTimeFixFunction(firstLocationTime)
        return firestoreDataPoints.chunked(4) {
            val fixedLocationTime = fixFunction(it[0].toLong())
            ActivityLocation(
                fixedLocationTime,
                it[1],
                it[2],
                it[3],
                0.0
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
