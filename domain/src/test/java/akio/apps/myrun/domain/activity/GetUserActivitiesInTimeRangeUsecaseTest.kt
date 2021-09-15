package akio.apps.myrun.domain.activity

import java.lang.AssertionError
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONDAY
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import org.junit.Test

class GetUserActivitiesInTimeRangeUsecaseTest {
    @Test
    fun testWeekRange() {
        val weekRange = GetUserActivitiesInTimeRangeUsecase.WeekRange() // this week
        val calendar = createPivotCalendar()
        calendar[Calendar.DAY_OF_WEEK] = MONDAY
        assertEquals(calendar.timeInMillis, weekRange.millisTimeRange.first)
        assertEquals(
            calendar.timeInMillis + TimeUnit.DAYS.toMillis(7), weekRange.millisTimeRange.last
        )
    }

    @Test
    fun testMonthRange_ThisMonth() {
        val monthRange = GetUserActivitiesInTimeRangeUsecase.MonthRange() // this month
        val calendar = createPivotCalendar()
        calendar[Calendar.DAY_OF_MONTH] = 1
        assertEquals(calendar.timeInMillis, monthRange.millisTimeRange.first)

        calendar[MONTH] = calendar[MONTH] + 1
        assertEquals(calendar.timeInMillis, monthRange.millisTimeRange.last)
    }

    @Test
    fun testMonthRange_WithOffsetAndCountUnder12() {
        val offset = 3
        val count = 2
        val monthRange = GetUserActivitiesInTimeRangeUsecase.MonthRange(offset, count)
        val localDateTime = LocalDateTime.now()
        var cursorDate = localDateTime.minusMonths(offset.toLong())
        val startMonth = cursorDate.month
        val startYear = cursorDate.year

        cursorDate = cursorDate.plusMonths(count.toLong())
        val endMonth = cursorDate.month
        val endYear = cursorDate.year

        val calendar = createPivotCalendar()
        calendar[DAY_OF_MONTH] = 1
        calendar[MONTH] = startMonth.value - 1
        calendar[YEAR] = startYear
        assertEquals(calendar.timeInMillis, monthRange.millisTimeRange.first)

        calendar[MONTH] = endMonth.value - 1
        calendar[YEAR] = endYear
        assertEquals(calendar.timeInMillis, monthRange.millisTimeRange.last)
    }

    @Test
    fun testMonthRange_WithOffsetAndCountOver12() {
        val offset = 23
        val count = 42
        val monthRange = GetUserActivitiesInTimeRangeUsecase.MonthRange(offset, count)
        val cursorCalendar = createPivotCalendar()
        cursorCalendar[DAY_OF_MONTH] = 1
        val localDateTime = LocalDateTime.now()
        var cursorDate = localDateTime.minusMonths(offset.toLong())
        cursorDate.setMonthYear(cursorCalendar)
        assertEquals(cursorCalendar.timeInMillis, monthRange.millisTimeRange.first)

        cursorDate = cursorDate.plusMonths(count.toLong())
        cursorDate.setMonthYear(cursorCalendar)
        assertEquals(cursorCalendar.timeInMillis, monthRange.millisTimeRange.last)
    }

    @Test
    fun testMonthRange_WithOffsetUnder12AndCountOver12() {
        val offset = 2
        val count = 37
        val monthRange = GetUserActivitiesInTimeRangeUsecase.MonthRange(offset, count)
        val cursorCalendar = createPivotCalendar()
        cursorCalendar[DAY_OF_MONTH] = 1
        val localDateTime = LocalDateTime.now()
        var cursorDate = localDateTime.minusMonths(offset.toLong())
        cursorDate.setMonthYear(cursorCalendar)
        assertEquals(cursorCalendar.timeInMillis, monthRange.millisTimeRange.first)

        cursorDate = cursorDate.plusMonths(count.toLong())
        cursorDate.setMonthYear(cursorCalendar)
        assertEquals(cursorCalendar.timeInMillis, monthRange.millisTimeRange.last)
    }

    @Test
    fun testMonthRange_WithOffsetOver12AndCountUnder12() {
        val offset = 27
        val count = 7
        val monthRange = GetUserActivitiesInTimeRangeUsecase.MonthRange(offset, count)
        val cursorCalendar = createPivotCalendar()
        cursorCalendar[DAY_OF_MONTH] = 1
        val localDateTime = LocalDateTime.now()
        var cursorDate = localDateTime.minusMonths(offset.toLong())
        cursorDate.setMonthYear(cursorCalendar)
        assertEquals(cursorCalendar.timeInMillis, monthRange.millisTimeRange.first)

        cursorDate = cursorDate.plusMonths(count.toLong())
        cursorDate.setMonthYear(cursorCalendar)
        assertEquals(cursorCalendar.timeInMillis, monthRange.millisTimeRange.last)
    }

    @Test(expected = AssertionError::class)
    fun testMonthRange_WithInvalidOffset() {
        GetUserActivitiesInTimeRangeUsecase.MonthRange(-1, 1)
    }

    @Test(expected = AssertionError::class)
    fun testMonthRange_WithInvalidCount() {
        GetUserActivitiesInTimeRangeUsecase.MonthRange(0, -1)
    }

    private fun createPivotCalendar(): Calendar = Calendar.getInstance().apply {
        this[Calendar.HOUR_OF_DAY] = 0
        this[Calendar.MINUTE] = 0
        this[Calendar.SECOND] = 0
        this[Calendar.MILLISECOND] = 0
    }

    private fun LocalDateTime.setMonthYear(calendar: Calendar) {
        calendar[MONTH] = monthValue - 1
        calendar[YEAR] = year
    }
}
