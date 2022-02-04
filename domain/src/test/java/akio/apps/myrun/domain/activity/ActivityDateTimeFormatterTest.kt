package akio.apps.myrun.domain.activity

import akio.apps.myrun.wiring.common.TimeProvider
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ActivityDateTimeFormatterTest {
    private lateinit var formatter: ActivityDateTimeFormatter
    private lateinit var mockedTimeProvider: TimeProvider
    private val rawOffset = 25200000 // GMT+7

    @Before
    fun setup() {
        mockedTimeProvider = mock()
        whenever(mockedTimeProvider.rawOffset()).thenReturn(rawOffset)
        formatter = ActivityDateTimeFormatter(mockedTimeProvider)
    }

    @Test
    fun test_WithinToday_EndOfSameDate() {
        // 25/12/2021 11:59 PM GMT+7
        whenever(mockedTimeProvider.currentTimeMillisWithOffset()).thenReturn(
            1640451540000 + rawOffset
        )
        // 25/12/2021 11:58 PM GMT+7
        val result = formatter.formatActivityDateTime(1640451480000)
        assertTrue(result is ActivityDateTimeFormatter.Result.WithinToday)
        assertEquals("11:58 PM", result.formattedValue)
    }

    @Test
    fun test_WithinYesterday_EndOfDayBefore_BeginOfDayAfter() {
        // 26/12/2021 12:01 AM GMT+7
        whenever(mockedTimeProvider.currentTimeMillisWithOffset()).thenReturn(
            1640451660000 + rawOffset
        )
        // 25/12/2021 11:58 PM GMT+7
        val result = formatter.formatActivityDateTime(1640451480000)
        assertTrue(result is ActivityDateTimeFormatter.Result.WithinYesterday)
        assertEquals("11:58 PM", result.formattedValue)
    }

    @Test
    fun test_WithinYesterday_EndOfDayBefore_EndOfDayAfter() {
        // 26/12/2021 11:58 PM GMT+7
        whenever(mockedTimeProvider.currentTimeMillisWithOffset()).thenReturn(
            1640537880000 + rawOffset
        )
        // 25/12/2021 11:58 PM GMT+7
        val result = formatter.formatActivityDateTime(1640451480000)
        assertTrue(result is ActivityDateTimeFormatter.Result.WithinYesterday)
        assertEquals("11:58 PM", result.formattedValue)
    }

    @Test
    fun test_WithinToday_EndAndBeginOfSameDate() {
        // 26/12/2021 11:58 PM GMT+7
        whenever(mockedTimeProvider.currentTimeMillisWithOffset()).thenReturn(
            1640537880000 + rawOffset
        )
        // 26/12/2021 12:01 AM GMT+7
        val result = formatter.formatActivityDateTime(1640451660000)
        assertTrue(result is ActivityDateTimeFormatter.Result.WithinToday)
        assertEquals("12:01 AM", result.formattedValue)
    }

    @Test
    fun test_FullDateTime() {
        // 27/12/2021 12:01 AM GMT+7
        whenever(mockedTimeProvider.currentTimeMillisWithOffset()).thenReturn(
            1640538060000 + rawOffset
        )
        // 25/12/2021 11:58 PM GMT+7
        val result = formatter.formatActivityDateTime(1640451480000)
        assertTrue(result is ActivityDateTimeFormatter.Result.FullDateTime)
        assertEquals("25/12/2021 11:58 PM", result.formattedValue)
    }
}
