package akio.apps.myrun.data.fitness.impl

import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.SingleDataPoint
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.FitnessOptions.ACCESS_WRITE
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FitnessDataRepositoryImpl @Inject constructor(
    private val appContext: Context
) : FitnessDataRepository {

    private val fitnessOptions
        get() = FitnessOptions.builder()
            .apply {
                dataTypes.forEach { addDataType(it, ACCESS_WRITE) }
            }
            .build()

    private val fitnessRecordingClient
        get() = GoogleSignIn.getAccountForExtension(appContext, fitnessOptions)
            .let { Fitness.getRecordingClient(appContext, it) }

    private val fitnessHistoryClient
        get() = GoogleSignIn.getAccountForExtension(appContext, fitnessOptions)
            .let { Fitness.getHistoryClient(appContext, it) }

    private val dataTypes = arrayOf(
        DataType.TYPE_SPEED,
        DataType.TYPE_STEP_COUNT_DELTA,
        DataType.TYPE_STEP_COUNT_CADENCE,
        DataType.TYPE_CYCLING_PEDALING_CADENCE,
        DataType.TYPE_CYCLING_PEDALING_CUMULATIVE,
        DataType.TYPE_CALORIES_EXPENDED,
        DataType.TYPE_HEART_RATE_BPM
    )

    override fun subscribeFitnessData() {
        try {
            dataTypes.forEach { fitnessRecordingClient.subscribe(it) }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    override fun unsubscribeFitnessData() {
        try {
            dataTypes.forEach { fitnessRecordingClient.unsubscribe(it) }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    private suspend fun readFitnessData(
        startTimeInSec: Long,
        endTimeInSec: Long,
        bucketTimeInSec: Long,
        dataType: DataType,
        aggregateType: DataType? = dataType.aggregateType
    ): List<DataPoint> {
        try {
            val readRequest = DataReadRequest.Builder().apply {
                if (aggregateType != null) {
                    aggregate(dataType)
                } else {
                    read(dataType)
                }
            }
                .bucketByTime(bucketTimeInSec.toInt(), TimeUnit.SECONDS)
                .setTimeRange(startTimeInSec, endTimeInSec, TimeUnit.SECONDS)
                .build()

            return fitnessHistoryClient.readData(readRequest).await()?.run {
                val dataPoints = mutableListOf<DataPoint>()
                if (buckets.isNotEmpty()) {
                    Timber.d("bucket count = ${buckets.size}")

                    for (bucket in buckets) {
                        Timber.d("dataset count = ${bucket.dataSets.size}")
                        for (dataset in bucket.dataSets) {
                            Timber.d("datapoint count = ${dataset.dataPoints.size}")
                            dataPoints.addAll(dataset.dataPoints)
                        }
                    }
                } else {
                    Timber.d("dataset count = ${dataSets.size}")
                    for (dataset in dataSets) {
                        Timber.d("datapoint count = ${dataset.dataPoints.size}")
                        dataPoints.addAll(dataset.dataPoints)
                    }
                }
                dataPoints.sortBy { it.getStartTime(TimeUnit.MILLISECONDS) }
                dataPoints
            } ?: emptyList()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            return emptyList()
        }
    }

    override suspend fun getSpeedDataPoints(startTime: Long, endTime: Long, interval: Long): List<SingleDataPoint<Float>> {
        val speedDataPoints = readFitnessData(startTime, endTime, interval, DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
        return speedDataPoints.map { SingleDataPoint(it.getStartTime(TimeUnit.MILLISECONDS), it.getValue(Field.FIELD_AVERAGE).asFloat()) }
    }

    private fun <V> mergeDataPoints(
        dp1: List<DataPoint>,
        dp1Converter: (DataPoint) -> SingleDataPoint<V>,
        dp2: List<DataPoint>,
        dp2Converter: (DataPoint) -> SingleDataPoint<V>
    ): List<SingleDataPoint<V>> {
        var i = 0
        var j = 0
        val mergeDataPoints = mutableListOf<SingleDataPoint<V>>()
        while (i < dp1.size || j < dp2.size) {
            val dp1StartTime = dp1.getOrNull(i)
                ?.getStartTime(TimeUnit.MILLISECONDS) ?: -1
            val dp2StartTime = dp2.getOrNull(j)
                ?.getStartTime(TimeUnit.MILLISECONDS) ?: -1
            if (dp2StartTime == -1L || dp1StartTime <= dp2StartTime) {
                mergeDataPoints.add(dp1Converter(dp1[i]))
                ++i
                if (dp1StartTime == dp2StartTime) {
                    ++j
                }
            } else if (dp1StartTime == -1L || dp1StartTime > dp2StartTime) {
                mergeDataPoints.add(dp2Converter(dp2[j]))
                ++j
            }
        }

        return mergeDataPoints
    }

    override suspend fun getSteppingCadenceDataPoints(startTime: Long, endTime: Long, interval: Long): List<SingleDataPoint<Int>> {
        val cadenceDataPoints = readFitnessData(startTime, endTime, interval, DataType.TYPE_STEP_COUNT_CADENCE)
        val stepDeltaDataPoints = readFitnessData(startTime, endTime, interval, DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
        return mergeDataPoints(
            cadenceDataPoints,
            { cadenceDp ->
                SingleDataPoint(
                    cadenceDp.getStartTime(TimeUnit.MILLISECONDS),
                    cadenceDp.getValue(Field.FIELD_RPM).asInt()
                )
            },
            stepDeltaDataPoints,
            { stepDeltaDp ->
                val value = stepDeltaDp.getValue(Field.FIELD_STEPS).asInt()
                val valueStartTime = stepDeltaDp.getStartTime(TimeUnit.MILLISECONDS)
                val valueEndTime = stepDeltaDp.getEndTime(TimeUnit.MILLISECONDS)
                val avgRpm: Int = ((60000f * value) / (valueEndTime - valueStartTime)).toInt()

                SingleDataPoint(
                    stepDeltaDp.getStartTime(TimeUnit.MILLISECONDS),
                    avgRpm
                )
            }
        )
    }

    override suspend fun getPedalingCadenceDataPoints(startTime: Long, endTime: Long, interval: Long): List<SingleDataPoint<Int>> {
        val cadenceDataPoints = readFitnessData(startTime, endTime, interval, DataType.TYPE_CYCLING_PEDALING_CADENCE)
        val cumulativeDataPoints = readFitnessData(startTime, endTime, interval, DataType.TYPE_CYCLING_PEDALING_CUMULATIVE)
        var lastCumulativeDp: DataPoint? = null
        return mergeDataPoints(
            cadenceDataPoints,
            { cadenceDp ->
                SingleDataPoint(
                    cadenceDp.getStartTime(TimeUnit.MILLISECONDS),
                    cadenceDp.getValue(Field.FIELD_RPM).asInt()
                )
            },
            cumulativeDataPoints,
            { cumulativeDp ->
                val endValue = cumulativeDp.getValue(Field.FIELD_REVOLUTIONS).asInt()
                val startValue = lastCumulativeDp?.getValue(Field.FIELD_REVOLUTIONS)?.asInt() ?: endValue   // fallback to endValue to make avg value = zero
                val cumulativeEndTime = cumulativeDp.getEndTime(TimeUnit.MILLISECONDS)
                val cumulativeStartTime = lastCumulativeDp?.getEndTime(TimeUnit.MILLISECONDS) ?: 0
                val avgRpm: Int = ((endValue - startValue) / ((cumulativeEndTime - cumulativeStartTime) / 60000f)).toInt()
                lastCumulativeDp = cumulativeDp
                SingleDataPoint(
                    cumulativeDp.getStartTime(TimeUnit.MILLISECONDS),
                    avgRpm
                )
            }
        )
    }

    override suspend fun getHeartRateDataPoints(startTime: Long, endTime: Long, interval: Long): List<SingleDataPoint<Int>> {
        val heartRateDataPoints = readFitnessData(startTime, endTime, interval, DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
        return heartRateDataPoints.map { SingleDataPoint(it.getStartTime(TimeUnit.MILLISECONDS), it.getValue(Field.FIELD_AVERAGE).asInt()) }
    }
}