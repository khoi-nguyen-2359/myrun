package akio.apps.myrun.domain.tracking

import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.domain.time.TimeProvider
import akio.apps.myrun.domain.tracking.locationprocessor.AverageLocationAccumulator
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
            AverageLocationAccumulator(
                accumulationPeriod,
                mockedTimeProvider
            )
    }

    @Test
    fun testAccumulate() {
        val batch1 = listOf(
            Location(0, 0, 1.0, 2.0, 3.0, 0.0),
            Location(4, 4, 5.0, 6.0, 7.0, 0.0),
            Location(8, 8, 9.0, 10.0, 11.0, 0.0),
        )
        whenever(mockedTimeProvider.currentTimeMillis()).thenReturn(500L)
        val firstDelivery = locationProcessorContainer.process(batch1)
        assertEquals(0, firstDelivery.size)

        val batch2 = listOf(
            Location(12, 12, 13.0, 14.0, 15.0, 0.0),
            Location(16, 16, 17.0, 18.0, 19.0, 0.0),
            Location(20, 20, 21.0, 22.0, 23.0, 0.0),
        )
        val avgLocationEntity2 = listOf(Location(20, 20, 11.0, 12.0, 13.0, 0.0))
        whenever(mockedTimeProvider.currentTimeMillis()).thenReturn(2000)
        val secondDelivery = locationProcessorContainer.process(batch2)
        assertEquals(avgLocationEntity2, secondDelivery)

        val batch3 = listOf(
            Location(0, 0, 2.0, 4.0, 3.0, 0.0),
            Location(4, 4, 4.0, 6.0, 7.0, 0.0),
            Location(8, 8, 6.0, 11.0, 11.0, 0.0),
        )
        whenever(mockedTimeProvider.currentTimeMillis()).thenReturn(2100)
        val thirdDelivery = locationProcessorContainer.process(batch3)
        assertEquals(0, thirdDelivery.size)

        val avgLocationEntity4 = Location(8, 8, 4.0, 7.0, 7.0, 0.0)
        val forthDelivery = locationProcessorContainer.deliverNow(2500)
        assertEquals(avgLocationEntity4, forthDelivery)
    }
}
