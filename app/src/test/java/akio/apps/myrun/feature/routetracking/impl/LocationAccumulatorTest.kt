package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.location.LocationEntity
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationAccumulatorTest {

    private lateinit var locationAccumulator: LocationAccumulator

    @Before
    fun setup() {
        locationAccumulator = LocationAccumulator(
            accumulateDuration = 2000L,
            startTime = 0L
        )
    }

    @Test
    fun testAccumulate() {
        val batch1 = listOf(
            LocationEntity(0, 1.0, 2.0, 3.0, 0.0),
            LocationEntity(4, 5.0, 6.0, 7.0, 0.0),
            LocationEntity(8, 9.0, 10.0, 11.0, 0.0),
        )
        val firstDelivery = locationAccumulator.accumulate(batch1, 500)
        assertNull(firstDelivery)

        val batch2 = listOf(
            LocationEntity(12, 13.0, 14.0, 15.0, 0.0),
            LocationEntity(16, 17.0, 18.0, 19.0, 0.0),
            LocationEntity(20, 21.0, 22.0, 23.0, 0.0),
        )
        val avgLocationEntity2 = LocationEntity(20, 11.0, 12.0, 13.0, 0.0)
        val secondDelivery = locationAccumulator.accumulate(batch2, 2000)
        assertEquals(avgLocationEntity2, secondDelivery)

        val batch3 = listOf(
            LocationEntity(0, 2.0, 4.0, 3.0, 0.0),
            LocationEntity(4, 4.0, 6.0, 7.0, 0.0),
            LocationEntity(8, 6.0, 11.0, 11.0, 0.0),
        )
        val thirdDelivery = locationAccumulator.accumulate(batch3, 2100)
        assertNull(thirdDelivery)

        val avgLocationEntity4 = LocationEntity(8, 4.0, 7.0, 7.0, 0.0)
        val forthDelivery = locationAccumulator.deliverNow(2500)
        assertEquals(avgLocationEntity4, forthDelivery)
    }
}
