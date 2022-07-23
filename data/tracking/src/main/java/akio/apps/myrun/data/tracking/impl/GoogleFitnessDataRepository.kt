package akio.apps.myrun.data.tracking.impl

import akio.apps.myrun.data.activity.api.model.DataPoint
import akio.apps.myrun.data.tracking.api.FitnessDataRepository
import akio.apps.myrun.data.tracking.di.TrackingDataScope
import android.app.Application
import androidx.annotation.VisibleForTesting
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.squareup.anvil.annotations.ContributesBinding
import java.util.TreeSet
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import timber.log.Timber

private typealias GmsDataPoint = com.google.android.gms.fitness.data.DataPoint

@Singleton
@ContributesBinding(TrackingDataScope::class)
class GoogleFitnessDataRepository @Inject constructor(
    private val application: Application,
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
        aggregateType: DataType? = dataType.aggregateType,
    ): List<GmsDataPoint> {
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

            return fitnessHistoryClient.readData(readRequest)
                .await()
                ?.run {
                    val dataPoints = TreeSet<GmsDataPoint> { o1, o2 ->
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
            return emptyList()
        }
    }

    override suspend fun getSpeedDataPoints(
        startTime: Long,
        endTime: Long,
        interval: Long,
    ): List<DataPoint<Float>> {
        val speedDataPoints = readFitnessData(
            startTime,
            endTime,
            interval,
            DataType.TYPE_SPEED,
            DataType.AGGREGATE_SPEED_SUMMARY
        )
        return speedDataPoints.map {
            DataPoint(
                it.getStartTime(TimeUnit.MILLISECONDS),
                it.getValue(Field.FIELD_AVERAGE)
                    .asFloat()
            )
        }
    }

    override suspend fun getSteppingCadenceDataPoints(
        startTime: Long,
        endTime: Long,
        interval: Long,
    ): List<DataPoint<Int>> {
        val cadenceDataPoints =
            readFitnessData(startTime, endTime, interval, DataType.TYPE_STEP_COUNT_CADENCE)
                .map { cadenceDp ->
                    DataPoint(
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

                DataPoint(
                    stepDeltaDp.getStartTime(TimeUnit.MILLISECONDS),
                    avgRpm
                )
            }

        return mergeDataPoints(cadenceDataPoints, stepDeltaDataPoints)
    }

    override suspend fun getHeartRateDataPoints(
        startTime: Long,
        endTime: Long,
        interval: Long,
    ): List<DataPoint<Int>> {
        val heartRateDataPoints = readFitnessData(
            startTime,
            endTime,
            interval,
            DataType.TYPE_HEART_RATE_BPM,
            DataType.AGGREGATE_HEART_RATE_SUMMARY
        )
        return heartRateDataPoints.map {
            DataPoint(
                it.getStartTime(TimeUnit.MILLISECONDS),
                it.getValue(Field.FIELD_AVERAGE)
                    .asInt()
            )
        }
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun <V> mergeDataPoints(
            dp1: List<DataPoint<V>>,
            dp2: List<DataPoint<V>>,
        ): List<DataPoint<V>> {
            var i = 0
            var j = 0
            val mergeDataPoints =
                mutableListOf<DataPoint<V>>()
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
