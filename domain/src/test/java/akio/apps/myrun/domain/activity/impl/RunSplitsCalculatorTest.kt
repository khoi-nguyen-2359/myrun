package akio.apps.myrun.domain.activity.impl

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.location.api.SphericalUtil
import akio.apps.myrun.domain.activity.RunSplitsCalculator
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RunSplitsCalculatorTest {
    private lateinit var runSplitsCalculator: RunSplitsCalculator

    private lateinit var mockedSphericalUtil: SphericalUtil

    @Before
    fun setup() {
        mockedSphericalUtil = mock()
        runSplitsCalculator = RunSplitsCalculator(mockedSphericalUtil)
    }

    @Test
    fun testCreateRunSplits_1FullSplit_1HalfSplit() {
        whenever(mockedSphericalUtil.computeDistanceBetween(any(), any()))
            .thenReturn(0.0, 300.0, 300.0, 300.0, 300.0)
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(0L, 2L, 3L, 300000L, 360000L)
                .map(::createActivityLocationWithElapsedTime)
        )
        verify(mockedSphericalUtil, times(5)).computeDistanceBetween(any(), any())
        assertEquals(2, runSplits.size)
        assertEquals(32 / 6.0, runSplits[0])
        assertEquals(20 / 6.0, runSplits[1])
    }

    @Test
    fun testCreateRunSplits_1HalfSplit() {
        whenever(mockedSphericalUtil.computeDistanceBetween(any(), any()))
            .thenReturn(0.0, 300.0, 300.0, 300.0)
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(0L, 2, 3, 300000)
                .map(::createActivityLocationWithElapsedTime)
        )
        assertEquals(1, runSplits.size)
        assertEquals("5.55555", String.format("%.5f", runSplits[0]))
    }

    @Test
    fun testCreateRunSplits_2MissingSplits_1HalfSplit() {
        whenever(mockedSphericalUtil.computeDistanceBetween(any(), any()))
            .thenReturn(0.0, 2200.0, 500.0)
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(0L, 620000, 770000)
                .map(::createActivityLocationWithElapsedTime)
        )
        verify(mockedSphericalUtil, times(3)).computeDistanceBetween(any(), any())
        assertEquals(3, runSplits.size)
        assertEquals("4.69697", String.format("%.5f", runSplits[0]))
        assertEquals("4.69697", String.format("%.5f", runSplits[1]))
        assertEquals("4.91342", String.format("%.5f", runSplits[2]))
    }

    @Test
    fun testCreateRunSplits_1RoundedSplit_2MissingSplits() {
        whenever(mockedSphericalUtil.computeDistanceBetween(any(), any()))
            .thenReturn(0.0, 1000.0, 2800.0)
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(0L, 300000, 1110000)
                .map(::createActivityLocationWithElapsedTime)
        )
        verify(mockedSphericalUtil, times(3)).computeDistanceBetween(any(), any())
        assertEquals(4, runSplits.size)
        assertEquals(5.0, runSplits[0])
        assertEquals("4.82143", String.format("%.5f", runSplits[1]))
        assertEquals("4.82143", String.format("%.5f", runSplits[2]))
        assertEquals("4.82142", String.format("%.5f", runSplits[3]))
    }

    @Test
    fun testCreateRunSplits_1RoundedSplit_2MissingSplits_PositiveActivityStartTime() {
        whenever(mockedSphericalUtil.computeDistanceBetween(any(), any()))
            .thenReturn(0.0, 1000.0, 2700.0)
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(1001L, 301001, 1111001)
                .map(::createActivityLocationWithElapsedTime)
        )
        verify(mockedSphericalUtil, times(3)).computeDistanceBetween(any(), any())
        assertEquals(4, runSplits.size)
        assertEquals("5.01668", String.format("%.5f", runSplits[0]))
        assertEquals("5.00000", String.format("%.5f", runSplits[1]))
        assertEquals("5.00000", String.format("%.5f", runSplits[2]))
        assertEquals("5.00000", String.format("%.5f", runSplits[3]))
    }

    private fun createActivityLocationWithElapsedTime(elapsedTime: Long): ActivityLocation =
        ActivityLocation(
            elapsedTime = elapsedTime,
            latitude = 0.0,
            longitude = 0.0,
            altitude = 0.0,
            speed = 0.0
        )
}
