package akio.apps.myrun.domain.user

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.wiring.common.NamedIoDispatcher
import akio.apps.myrun.wiring.common.Now
import akio.apps.myrun.wiring.common.TimeProvider
import android.os.Parcelable
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.MONDAY
import java.util.Calendar.MONTH
import java.util.Calendar.SECOND
import java.util.Calendar.YEAR
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

class GetTrainingSummaryDataUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val timeProvider: TimeProvider = Now
) {
    /**
     * [weekOffset]: value >= 0, indicates current week, or 1, 2, 3... week in the past.
     */
    suspend fun getUserTrainingSummaryData(): Map<ActivityType, TrainingSummaryTableData> =
        withContext(ioDispatcher) {
            val currentTime = timeProvider.currentTimeMillis()
            val biMonthRange = MonthRange(offset = 1, count = 2, currentTime)
            val userId = userAuthenticationState.requireUserAccountId()
            val activitiesInRange = activityRepository.getActivitiesInTimeRange(
                userId,
                biMonthRange.millisTimeRange.first,
                biMonthRange.millisTimeRange.last
            )
            listOf(ActivityType.Running, ActivityType.Cycling).map { activityType ->
                val thisWeekData =
                    WeekRange(time = currentTime) to mutableListOf<BaseActivityModel>()
                val lastWeekData =
                    WeekRange(offset = 1, time = currentTime) to mutableListOf<BaseActivityModel>()
                val thisMonthData =
                    MonthRange(time = currentTime) to mutableListOf<BaseActivityModel>()
                val lastMonthData =
                    MonthRange(offset = 1, time = currentTime) to mutableListOf<BaseActivityModel>()
                val timeRangeDataList =
                    listOf(thisWeekData, lastWeekData, thisMonthData, lastMonthData)
                activitiesInRange
                    .filter { it.activityType == activityType }
                    .forEach { activity ->
                        timeRangeDataList.forEach { (timeRange, activityList) ->
                            if (activity.startTime in timeRange.millisTimeRange) {
                                activityList.add(activity)
                            }
                        }
                    }

                var summaryTable = TrainingSummaryTableData()
                timeRangeDataList.forEach { (timeRange, activityList) ->
                    val summaryData = TrainingSummaryData(
                        distance = activityList.sumOf { it.distance },
                        time = activityList.sumOf { it.duration },
                        activityCount = activityList.size
                    )
                    summaryTable = when {
                        timeRange is WeekRange && timeRange.offset == 0 -> summaryTable.copy(
                            thisWeekSummary = summaryData
                        )
                        timeRange is WeekRange && timeRange.offset == 1 -> summaryTable.copy(
                            lastWeekSummary = summaryData
                        )
                        timeRange is MonthRange && timeRange.offset == 0 -> summaryTable.copy(
                            thisMonthSummary = summaryData
                        )
                        timeRange is MonthRange && timeRange.offset == 1 -> summaryTable.copy(
                            lastMonthSummary = summaryData
                        )
                        else -> summaryTable
                    }
                }

                activityType to summaryTable
            }.toMap()
        }

    @Parcelize
    data class TrainingSummaryTableData(
        val thisWeekSummary: TrainingSummaryData = TrainingSummaryData(),
        val lastWeekSummary: TrainingSummaryData = TrainingSummaryData(),
        val thisMonthSummary: TrainingSummaryData = TrainingSummaryData(),
        val lastMonthSummary: TrainingSummaryData = TrainingSummaryData(),
    ) : Parcelable

    @Parcelize
    data class TrainingSummaryData(
        val distance: Double = 0.0,
        val time: Long = 0L,
        val activityCount: Int = 0,
    ) : Parcelable

    abstract class TimeRange(
        val offset: Int,
        val count: Int,
        protected val time: Long,
    ) {
        init {
            assert(offset >= 0)
            assert(count >= 0)
        }

        abstract val millisTimeRange: LongRange
        fun firstHalf(): TimeRange = instantiate(offset, count / 2)
        fun secondHalf(): TimeRange = instantiate(offset - count / 2, count / 2)
        abstract fun instantiate(offset: Int, count: Int): TimeRange
    }

    class WeekRange(offset: Int = 0, count: Int = 1, time: Long = Now.currentTimeMillis()) :
        TimeRange(offset, count, time) {
        override val millisTimeRange: LongRange = run {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            calendar.firstDayOfWeek = MONDAY
            calendar[DAY_OF_WEEK] = MONDAY
            calendar[HOUR_OF_DAY] = 0
            calendar[MINUTE] = 0
            calendar[SECOND] = 0
            calendar[MILLISECOND] = 0
            val weekInMillis = TimeUnit.DAYS.toMillis(7)
            val startRange = calendar.timeInMillis - offset * weekInMillis
            LongRange(startRange, startRange + weekInMillis * count)
        }

        override fun instantiate(offset: Int, count: Int): WeekRange = WeekRange(offset, count)
    }

    class MonthRange(offset: Int = 0, count: Int = 1, time: Long = Now.currentTimeMillis()) :
        TimeRange(offset, count, time) {
        override val millisTimeRange: LongRange = run {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            calendar[DAY_OF_MONTH] = 1
            calendar[HOUR_OF_DAY] = 0
            calendar[MINUTE] = 0
            calendar[SECOND] = 0
            calendar[MILLISECOND] = 0
            calendar.plusMonth(-offset)
            val startRange = calendar.timeInMillis
            calendar.plusMonth(count)
            LongRange(startRange, calendar.timeInMillis)
        }

        override fun instantiate(offset: Int, count: Int): MonthRange = MonthRange(offset, count)

        private fun Calendar.plusMonth(count: Int) {
            val monthCount = this[YEAR] * 12 + this[MONTH]
            val sumMonth = monthCount + count
            this[MONTH] = sumMonth % 12
            this[YEAR] = sumMonth / 12
        }
    }
}
