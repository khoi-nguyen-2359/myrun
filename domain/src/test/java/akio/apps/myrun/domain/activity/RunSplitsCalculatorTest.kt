package akio.apps.myrun.domain.activity

import akio.apps.myrun._base.utils.GmsLatLng
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import com.google.maps.android.SphericalUtil
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class RunSplitsCalculatorTest {
    private lateinit var runSplitsCalculator: RunSplitsCalculator
    private val originLatLng: GmsLatLng = GmsLatLng(0.0, 0.0)

    @Before
    fun setup() {
        runSplitsCalculator = RunSplitsCalculator()
    }

    @Test
    fun testCreateRunSplits_1FullSplit_1HalfSplit() {
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(
                createNextStopFromOrigin(0, 0.0),
                createNextStopFromOrigin(2, 300.0),
                createNextStopFromOrigin(3, 600.0),
                createNextStopFromOrigin(300_000, 900.0),
                createNextStopFromOrigin(360_000, 1200.0)
            )
        )
        assertEquals(2, runSplits.size)
        assertEquals(32 / 6.0, runSplits[0])
        assertEquals(20 / 6.0, runSplits[1])
    }

    @Test
    fun testCreateRunSplits_1HalfSplit() {
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(
                createNextStopFromOrigin(0, 0.0),
                createNextStopFromOrigin(2, 300.0),
                createNextStopFromOrigin(3, 600.0),
                createNextStopFromOrigin(300_000, 900.0)
            )
        )
        assertEquals(1, runSplits.size)
        assertEquals("5.55555", String.format("%.5f", runSplits[0]))
    }

    @Test
    fun testCreateRunSplits_2MissingSplits_1HalfSplit() {
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(
                createNextStopFromOrigin(0, 0.0),
                createNextStopFromOrigin(620_000, 2200.0),
                createNextStopFromOrigin(770_000, 2700.0)
            )
        )
        assertEquals(3, runSplits.size)
        assertEquals("4.69697", String.format("%.5f", runSplits[0]))
        assertEquals("4.69697", String.format("%.5f", runSplits[1]))
        assertEquals("4.91342", String.format("%.5f", runSplits[2]))
    }

    @Test
    fun testCreateRunSplits_1RoundedSplit_2MissingSplits() {
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(
                createNextStopFromOrigin(0, 0.0),
                createNextStopFromOrigin(300_000, 1000.0),
                createNextStopFromOrigin(1_110_000, 3700.0)
            )
        )
        assertEquals(4, runSplits.size)
        assertEquals(5.0, runSplits[0])
        assertEquals("4.99999", String.format("%.5f", runSplits[1]))
        assertEquals("4.99999", String.format("%.5f", runSplits[2]))
        assertEquals("4.99998", String.format("%.5f", runSplits[3]))
    }

    @Test
    fun testCreateRunSplits_1RoundedSplit_2MissingSplits_PositiveActivityStartTime() {
        val runSplits = runSplitsCalculator.createRunSplits(
            locationDataPoints = listOf(
                createNextStopFromOrigin(1001, 0.0),
                createNextStopFromOrigin(301_001, 1000.0),
                createNextStopFromOrigin(1_111_001, 3700.0)
            )
        )
        assertEquals(4, runSplits.size)
        assertEquals("5.01668", String.format("%.5f", runSplits[0]))
        assertEquals("4.99999", String.format("%.5f", runSplits[1]))
        assertEquals("4.99999", String.format("%.5f", runSplits[2]))
        assertEquals("4.99998", String.format("%.5f", runSplits[3]))
    }

    /**
     * On the line of origin(0,0) with heading=0.0, choose a point from origin an offset=[offset]
     */
    private fun createNextStopFromOrigin(locationTime: Long, offset: Double): ActivityLocation {
        val destination = SphericalUtil.computeOffset(originLatLng, offset, 0.0)
        return ActivityLocation(
            elapsedTime = locationTime,
            latitude = destination.latitude,
            longitude = destination.longitude,
            altitude = 0.0,
            speed = 0.0
        )
    }
}
