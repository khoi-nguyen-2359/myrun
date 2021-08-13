package akio.apps.myrun.data.fitness.impl

import akio.apps.myrun.data.fitness.DataPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleFitnessDataRepositoryTest {

    @Test
    fun testDataPointsMerger() {
        val dp1 = listOf(
            DataPoint(0, 0.0),
            DataPoint(1, 1.0),
            DataPoint(2, 2.0),
            DataPoint(4, 4.0),
            DataPoint(7, 7.1),
            DataPoint(8, 8.0),
            DataPoint(9, 9.0),
            DataPoint(12, 12.0)
        )
        val dp2 = listOf(
            DataPoint(3, 3.0),
            DataPoint(5, 5.0),
            DataPoint(7, 7.2),
            DataPoint(10, 10.0),
            DataPoint(11, 11.0)
        )
        val merged = GoogleFitnessDataRepository.mergeDataPoints(dp1, dp2)
        assertEquals(12, merged.size)
        assertEquals(0.0, merged[0].value, 0.0)
        assertEquals(1.0, merged[1].value, 0.0)
        assertEquals(2.0, merged[2].value, 0.0)
        assertEquals(3.0, merged[3].value, 0.0)
        assertEquals(4.0, merged[4].value, 0.0)
        assertEquals(5.0, merged[5].value, 0.0)
        assertEquals(7.1, merged[6].value, 0.0)
        assertEquals(8.0, merged[7].value, 0.0)
        assertEquals(9.0, merged[8].value, 0.0)
        assertEquals(10.0, merged[9].value, 0.0)
        assertEquals(11.0, merged[10].value, 0.0)
        assertEquals(12.0, merged[11].value, 0.0)
    }
}
