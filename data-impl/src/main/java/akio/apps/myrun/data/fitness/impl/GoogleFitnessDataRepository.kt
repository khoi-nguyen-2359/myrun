package akio.apps.myrun.data.fitness.impl

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.fitness.FitnessDataRepository
import android.app.Application
import androidx.annotation.VisibleForTesting
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.TreeSet
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GoogleFitnessDataRepository @Inject constructor(
    private val application: Application,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FitnessDataRepository {

    private val fitnessOptions
        get() = FitnessOptions.builder()
            .apply { dataTypes.forEach(::addDataType) }
            .build()

    private val fitnessRecordingClient
        get() = GoogleSignIn.getAccountForExtension(application, fitnessOptions)
            .let { Fitness.getRecordingClient(application, it) }

    private val fitnessHistoryClient
        get() = GoogleSignIn.getAccountForExtension(application, fitnessOptions)
            .let { Fitness.getHistoryClient(application, it) }

    private val dataTypes = arrayOf(
        DataType.TYPE_SPEED,
        DataType.TYPE_STEP_COUNT_DELTA,
        DataType.TYPE_STEP_COUNT_CADENCE,
        DataType.TYPE_STEP_COUNT_CUMULATIVE
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
    ): List<DataPoint> = withContext(ioDispatcher) {
        try {
            val readRequest = DataReadRequest.Builder()
                .apply {
                    if (aggregateType != null) {
                        aggregate(dataType)
                    } else {
                        read(dataType)
                    }
                }
                .bucketByTime(bucketTimeInSec.toInt(), TimeUnit.SECONDS)
                .setTimeRange(startTimeInSec, endTimeInSec, TimeUnit.SECONDS)
                .build()

            return@withContext fitnessHistoryClient.readData(readRequest)
                .await()
                ?.run {
                    val dataPoints = TreeSet<DataPoint> { o1, o2 ->
                        val startTime1 = o1.getStartTime(TimeUnit.MILLISECONDS)
                        val startTime2 = o2.getStartTime(TimeUnit.MILLISECONDS)
                        (startTime1 - startTime2).toInt()
                    }
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
                    dataPoints.toList()
                }
                ?: emptyList()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            return@withContext emptyList()
        }
    }

    override suspend fun getSpeedDataPoints(
        startTime: Long,
        endTime: Long,
        interval: Long
    ): List<akio.apps.myrun.data.fitness.DataPoint<Float>> = withContext(ioDispatcher) {
        val speedDataPoints = readFitnessData(
            startTime,
            endTime,
            interval,
            DataType.TYPE_SPEED,
            DataType.AGGREGATE_SPEED_SUMMARY
        )
        return@withContext speedDataPoints.map {
            akio.apps.myrun.data.fitness.DataPoint(
                it.getStartTime(TimeUnit.MILLISECONDS),
                it.getValue(Field.FIELD_AVERAGE)
                    .asFloat()
            )
        }
    }

    override suspend fun getSteppingCadenceDataPoints(
        startTime: Long,
        endTime: Long,
        interval: Long
    ): List<akio.apps.myrun.data.fitness.DataPoint<Int>> = withContext(ioDispatcher) {
        val cadenceDataPoints =
            readFitnessData(startTime, endTime, interval, DataType.TYPE_STEP_COUNT_CADENCE)
                .map { cadenceDp ->
                    akio.apps.myrun.data.fitness.DataPoint(
                        cadenceDp.getStartTime(TimeUnit.MILLISECONDS),
                        cadenceDp.getValue(Field.FIELD_RPM)
                            .asInt()
                    )
                }

        val stepDeltaDataPoints = readFitnessData(
            startTime,
            endTime,
            interval,
            DataType.TYPE_STEP_COUNT_DELTA,
            DataType.AGGREGATE_STEP_COUNT_DELTA
        )
            .map { stepDeltaDp ->
                val value = stepDeltaDp.getValue(Field.FIELD_STEPS)
                    .asInt()
                val valueStartTime = stepDeltaDp.getStartTime(TimeUnit.MILLISECONDS)
                val valueEndTime = stepDeltaDp.getEndTime(TimeUnit.MILLISECONDS)
                val avgRpm: Int = ((60000f * value) / (valueEndTime - valueStartTime)).toInt()

                akio.apps.myrun.data.fitness.DataPoint(
                    stepDeltaDp.getStartTime(TimeUnit.MILLISECONDS),
                    avgRpm
                )
            }

        return@withContext mergeDataPoints(cadenceDataPoints, stepDeltaDataPoints)
    }

    override suspend fun getHeartRateDataPoints(
        startTime: Long,
        endTime: Long,
        interval: Long
    ): List<akio.apps.myrun.data.fitness.DataPoint<Int>> = withContext(ioDispatcher) {
        val heartRateDataPoints = readFitnessData(
            startTime,
            endTime,
            interval,
            DataType.TYPE_HEART_RATE_BPM,
            DataType.AGGREGATE_HEART_RATE_SUMMARY
        )
        return@withContext heartRateDataPoints.map {
            akio.apps.myrun.data.fitness.DataPoint(
                it.getStartTime(TimeUnit.MILLISECONDS),
                it.getValue(Field.FIELD_AVERAGE)
                    .asInt()
            )
        }
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun <V> mergeDataPoints(
            dp1: List<akio.apps.myrun.data.fitness.DataPoint<V>>,
            dp2: List<akio.apps.myrun.data.fitness.DataPoint<V>>,
        ): List<akio.apps.myrun.data.fitness.DataPoint<V>> {
            var i = 0
            var j = 0
            val mergeDataPoints = mutableListOf<akio.apps.myrun.data.fitness.DataPoint<V>>()
            while (i < dp1.size || j < dp2.size) {
                val dp1StartTime = dp1.getOrNull(i)
                    ?.timestamp
                val dp2StartTime = dp2.getOrNull(j)
                    ?.timestamp
                if (dp2StartTime == null ||
                    (dp1StartTime != null && dp1StartTime <= dp2StartTime)
                ) {
                    mergeDataPoints.add(dp1[i])
                    ++i
                    if (dp1StartTime == dp2StartTime) {
                        ++j
                    }
                } else if (dp1StartTime == null || dp1StartTime > dp2StartTime) {
                    mergeDataPoints.add(dp2[j])
                    ++j
                }
            }

            return mergeDataPoints
        }
    }
}
