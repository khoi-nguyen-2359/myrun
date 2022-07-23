package akio.apps.myrun.data.tracking.api

interface FitnessDataRepository {
    fun subscribeFitnessData()
    fun unsubscribeFitnessData()
    suspend fun getSpeedDataPoints(
        startTime: Long,
        endTime: Long,
        interval: Long,
    ): List<akio.apps.myrun.data.activity.api.model.DataPoint<Float>>

    suspend fun getSteppingCadenceDataPoints(
        startTime: Long,
        endTime: Long,
        interval: Long,
    ): List<akio.apps.myrun.data.activity.api.model.DataPoint<Int>>

    suspend fun getHeartRateDataPoints(
        startTime: Long,
        endTime: Long,
        interval: Long,
    ): List<akio.apps.myrun.data.activity.api.model.DataPoint<Int>>
}
