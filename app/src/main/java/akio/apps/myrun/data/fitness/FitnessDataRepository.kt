package akio.apps.myrun.data.fitness

interface FitnessDataRepository {
    fun subscribeFitnessData()
    fun unsubscribeFitnessData()
    suspend fun getSpeedDataPoints(startTime: Long, endTime: Long, interval: Long): List<SingleDataPoint<Float>>
    suspend fun getSteppingCadenceDataPoints(startTime: Long, endTime: Long, interval: Long): List<SingleDataPoint<Int>>
    suspend fun getHeartRateDataPoints(startTime: Long, endTime: Long, interval: Long): List<SingleDataPoint<Int>>
}