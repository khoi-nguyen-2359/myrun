package akio.apps.myrun.domain.activity

import akio.apps.common.wiring.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber

class GetTrainingSummaryDataUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    /**
     * [weekOffset]: value >= 0, indicates current week, or 1, 2, 3... week in the past.
     */
    @OptIn(FlowPreview::class)
    suspend fun getUserTrainingSummaryData(): Map<ActivityType, TrainingSummaryTableData> =
        withContext(ioDispatcher) {
            val userId = userAuthenticationState.requireUserAccountId()
            val timeRangeList = listOf(
                WeekRange(offset = 1, count = 2),
                MonthRange(offset = 1, count = 2),
            )
            val activityTypeList = listOf(
                ActivityType.Running,
                ActivityType.Cycling
            )
            val activityParamList: List<Pair<TimeRange, ActivityType>> =
                timeRangeList.map { timeRange ->
                    activityTypeList.map { activityType -> timeRange to activityType }
                }.flatten()
            activityParamList.asFlow()
                .flatMapMerge { (timeRange, activityType) ->
                    flow {
                        val activitiesInRange = activityRepository.getActivitiesInTimeRange(
                            userId,
                            activityType,
                            timeRange.millisTimeRange.first,
                            timeRange.millisTimeRange.last
                        )
                        Timber.d("activitiesInRange type=$activityType, range=$timeRange")
                        val firstHalfRange = timeRange.firstHalf()
                        val midActivityIndex = activitiesInRange.indexOfFirst {
                            it.startTime < firstHalfRange.millisTimeRange.last
                        }
                        if (midActivityIndex == -1)
                            return@flow

                        emit(
                            Triple(
                                firstHalfRange,
                                activityType,
                                activitiesInRange.subList(midActivityIndex, activitiesInRange.size)
                            )
                        )
                        emit(
                            Triple(
                                timeRange.secondHalf(),
                                activityType,
                                activitiesInRange.subList(0, midActivityIndex)
                            )
                        )
                    }
                }.fold(
                    initial = mutableMapOf(
                        ActivityType.Running to TrainingSummaryTableData(),
                        ActivityType.Cycling to TrainingSummaryTableData()
                    ),
                    { accum, (timeRange, activityType, activitiesInRange) ->
                        val summaryTable = accum[activityType] ?: return@fold accum
                        val summaryData = TrainingSummaryData(
                            distance = activitiesInRange.sumOf { it.distance },
                            time = activitiesInRange.sumOf { it.duration },
                            activityCount = activitiesInRange.size
                        )
                        accum[activityType] = when {
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
                        accum
                    }
                )
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

    abstract class TimeRange(val offset: Int, val count: Int) {
        init {
            assert(offset >= 0)
            assert(count >= 0)
        }

        abstract val millisTimeRange: LongRange
        fun firstHalf(): TimeRange = instantiate(offset, count / 2)
        fun secondHalf(): TimeRange = instantiate(offset - count / 2, count / 2)
        abstract fun instantiate(offset: Int, count: Int): TimeRange
    }

    class WeekRange(offset: Int = 0, count: Int = 1) : TimeRange(offset, count) {
        override val millisTimeRange: LongRange = run {
            val calendar = Calendar.getInstance()
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

    class MonthRange(offset: Int = 0, count: Int = 1) : TimeRange(offset, count) {
        override val millisTimeRange: LongRange = run {
            val calendar = Calendar.getInstance()
            calendar[DAY_OF_MONTH] = 1
            calendar[HOUR_OF_DAY] = 0
            calendar[MINUTE] = 0
            calendar[SECOND] = 0
            calendar[MILLISECOND] = 0
            calendar.minusMonth(offset)
            val startRange = calendar.timeInMillis
            calendar.plusMonth(count)
            LongRange(startRange, calendar.timeInMillis)
        }

        override fun instantiate(offset: Int, count: Int): MonthRange = MonthRange(offset, count)

        private fun Calendar.plusMonth(count: Int) {
            assert(count >= 0)
            val sumMonth = this[MONTH] + count
            this[MONTH] = sumMonth % 12
            this[YEAR] += sumMonth / 12
        }

        private fun Calendar.minusMonth(count: Int) {
            assert(count >= 0)
            val sumMonth = this[MONTH] - count
            this[MONTH] = (sumMonth % 12 + 12) % 12
            this[YEAR] += if (sumMonth >= 0) {
                0
            } else {
                (sumMonth - 12) / 12
            }
        }
    }
}
