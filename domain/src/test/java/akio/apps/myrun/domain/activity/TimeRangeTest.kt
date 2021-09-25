package akio.apps.myrun.domain.activity

import java.time.LocalDateTime
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONDAY
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import org.junit.Test

class TimeRangeTest {
    @Test
    fun testWeekRange() {
        val weekRange = GetTrainingSummaryDataUsecase.WeekRange() // this week
        val calendar = createPivotCalendar()
        calendar[Calendar.DAY_OF_WEEK] = MONDAY
        assertEquals(calendar.timeInMillis, weekRange.millisTimeRange.first)
        assertEquals(
            calendar.timeInMillis + TimeUnit.DAYS.toMillis(7), weekRange.millisTimeRange.last
        )
    }

    @Test
    fun testMonthRange_ThisMonth() {
        val monthRange = GetTrainingSummaryDataUsecase.MonthRange() // this month
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
        val monthRange = GetTrainingSummaryDataUsecase.MonthRange(offset, count)
        val localDateTimeRange = createMonthRangeUsingLocalDate(offset, count)
        assertEquals(localDateTimeRange.first, monthRange.millisTimeRange.first)
        assertEquals(localDateTimeRange.last, monthRange.millisTimeRange.last)
    }

    @Test
    fun testMonthRange_WithOffsetAndCountOver12() {
        val offset = 23
        val count = 42
        val monthRange = GetTrainingSummaryDataUsecase.MonthRange(offset, count)
        val localDateTimeRange = createMonthRangeUsingLocalDate(offset, count)
        assertEquals(localDateTimeRange.first, monthRange.millisTimeRange.first)
        assertEquals(localDateTimeRange.last, monthRange.millisTimeRange.last)
    }

    @Test
    fun testMonthRange_WithOffsetUnder12AndCountOver12() {
        val offset = 2
        val count = 37
        val monthRange = GetTrainingSummaryDataUsecase.MonthRange(offset, count)
        val localDateTimeRange = createMonthRangeUsingLocalDate(offset, count)
        assertEquals(localDateTimeRange.first, monthRange.millisTimeRange.first)
        assertEquals(localDateTimeRange.last, monthRange.millisTimeRange.last)
    }

    @Test
    fun testMonthRange_WithOffsetOver12AndCountUnder12() {
        val offset = 27
        val count = 7
        val monthRange = GetTrainingSummaryDataUsecase.MonthRange(offset, count)
        val localDateTimeRange = createMonthRangeUsingLocalDate(offset, count)
        assertEquals(localDateTimeRange.first, monthRange.millisTimeRange.first)
        assertEquals(localDateTimeRange.last, monthRange.millisTimeRange.last)
    }

    @Test
    fun testMonthRange_FirstHalf() {
        val offset = 1
        val count = 2
        val monthRange = GetTrainingSummaryDataUsecase.MonthRange(offset, count)
        val firstHalf = monthRange.firstHalf()
        val localDateTimeRange = createMonthRangeUsingLocalDate(offset, count / 2)
        assertEquals(localDateTimeRange.first, firstHalf.millisTimeRange.first)
        assertEquals(localDateTimeRange.last, firstHalf.millisTimeRange.last)
    }

    @Test
    fun testWeekRange_SecondHalf() {
        val offset = 1
        val count = 2
        val monthRange = GetTrainingSummaryDataUsecase.WeekRange(offset, count)
        val secondHalf = monthRange.secondHalf()
        val calendar = createPivotCalendar()
        calendar[Calendar.DAY_OF_WEEK] = MONDAY
        assertEquals(calendar.timeInMillis, secondHalf.millisTimeRange.first)
        assertEquals(
            calendar.timeInMillis + TimeUnit.DAYS.toMillis(7),
            secondHalf.millisTimeRange.last
        )
    }

    @Test(expected = AssertionError::class)
    fun testMonthRange_WithInvalidOffset() {
        GetTrainingSummaryDataUsecase.MonthRange(-1, 1)
    }

    @Test(expected = AssertionError::class)
    fun testMonthRange_WithInvalidCount() {
        GetTrainingSummaryDataUsecase.MonthRange(0, -1)
    }

    private fun createMonthRangeUsingLocalDate(offset: Int, count: Int): LongRange {
        val cursorCalendar = createPivotCalendar()
        cursorCalendar[DAY_OF_MONTH] = 1
        val localDateTime = LocalDateTime.now()
        var cursorDate = localDateTime.minusMonths(offset.toLong())
        cursorDate.setMonthYear(cursorCalendar)
        val startTime = cursorCalendar.timeInMillis
        cursorDate = cursorDate.plusMonths(count.toLong())
        cursorDate.setMonthYear(cursorCalendar)
        return LongRange(startTime, cursorCalendar.timeInMillis)
    }

    private fun createPivotCalendar(): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone("Z")).apply {
            firstDayOfWeek = MONDAY
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
