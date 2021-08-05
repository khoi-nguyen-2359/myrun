package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.time.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AverageLocationAccumulatorTest {

    private lateinit var locationProcessorContainer: AverageLocationAccumulator

    private val accumulationPeriod = 2000L
    private lateinit var mockedTimeProvider: TimeProvider

    @Before
    fun setup() {
        mockedTimeProvider = mock()
        locationProcessorContainer =
            AverageLocationAccumulator(accumulationPeriod, mockedTimeProvider)
    }

    @Test
    fun testAccumulate() {
        val batch1 = listOf(
            LocationEntity(0, 1.0, 2.0, 3.0, 0.0),
            LocationEntity(4, 5.0, 6.0, 7.0, 0.0),
            LocationEntity(8, 9.0, 10.0, 11.0, 0.0),
        )
        whenever(mockedTimeProvider.currentMillisecond()).thenReturn(500L)
        val firstDelivery = locationProcessorContainer.process(batch1)
        assertEquals(0, firstDelivery.size)

        val batch2 = listOf(
            LocationEntity(12, 13.0, 14.0, 15.0, 0.0),
            LocationEntity(16, 17.0, 18.0, 19.0, 0.0),
            LocationEntity(20, 21.0, 22.0, 23.0, 0.0),
        )
        val avgLocationEntity2 = listOf(LocationEntity(20, 11.0, 12.0, 13.0, 0.0))
        whenever(mockedTimeProvider.currentMillisecond()).thenReturn(2000)
        val secondDelivery = locationProcessorContainer.process(batch2)
        assertEquals(avgLocationEntity2, secondDelivery)

        val batch3 = listOf(
            LocationEntity(0, 2.0, 4.0, 3.0, 0.0),
            LocationEntity(4, 4.0, 6.0, 7.0, 0.0),
            LocationEntity(8, 6.0, 11.0, 11.0, 0.0),
        )
        whenever(mockedTimeProvider.currentMillisecond()).thenReturn(2100)
        val thirdDelivery = locationProcessorContainer.process(batch3)
        assertEquals(0, thirdDelivery.size)

        val avgLocationEntity4 = LocationEntity(8, 4.0, 7.0, 7.0, 0.0)
        val forthDelivery = locationProcessorContainer.deliverNow(2500)
        assertEquals(avgLocationEntity4, forthDelivery)
    }
}
