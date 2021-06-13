package akio.apps.myrun.data.activity.entity

import akio.apps.myrun.data.fitness.DataPoint
import akio.apps.myrun.data.location.LocationEntity
import com.google.firebase.firestore.PropertyName

data class FirestoreDataPointList(
    @JvmField @PropertyName("data")
    val data: List<Double> = emptyList()
)

class FirestoreDataPointSerializer<T>(
    private val firestoreDataPointParser: FirestoreDataPointParser<T>
) {
    fun serialize(dataPoints: List<DataPoint<T>>): FirestoreDataPointList {
        return FirestoreDataPointList(dataPoints.flatMap { firestoreDataPointParser.flatten(it) })
    }
}

class FirestoreDataPointDeserializer<T>(
    private val dataPointParser: FirestoreDataPointParser<T>
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

class FirestoreLocationDataPointParser : FirestoreDataPointParser<LocationEntity> {
    override fun flatten(dataPoint: DataPoint<LocationEntity>): List<Double> = listOf(
        dataPoint.timestamp.toDouble(),
        dataPoint.value.latitude,
        dataPoint.value.longitude,
        dataPoint.value.altitude
    )

    override fun build(firestoreDataIterator: Iterator<Double>): LocationEntity {
        val latitude = firestoreDataIterator.next()
        val longitude = firestoreDataIterator.next()
        val altitude = firestoreDataIterator.next()

        return LocationEntity(latitude, longitude, altitude)
    }
}
