package akio.apps.myrun.domain.activity

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
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

class GetUserActivitiesInTimeRangeUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
) {

    /**
     * [weekOffset]: value >= 0, indicates current week, or 1, 2, 3... week in the past.
     */
    fun getUserActivitiesInAWeek(weekOffset: Int) {
        val calendar = Calendar.getInstance()
        calendar[DAY_OF_WEEK] = MONDAY
        calendar[HOUR_OF_DAY] = 0
        calendar[MINUTE] = 0
        calendar[SECOND] = 0
        calendar[MILLISECOND] = 0
    }

    abstract class TimeRange(val offset: Int, val count: Int) {
        init {
            assert(offset >= 0)
            assert(count >= 0)
        }

        abstract val millisTimeRange: LongRange
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
