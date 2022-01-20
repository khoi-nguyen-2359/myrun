package akio.apps.myrun.feature.route.model

import akio.apps.myrun.data.location.api.model.LatLng
import kotlin.test.assertEquals
import org.junit.Test

class RoutePlottingStateTest {

    private lateinit var routePlottingState: RoutePlottingState

    @Test
    fun testRecord_emptyInitialState() {
        routePlottingState = RoutePlottingState(
            stateData = emptyList(),
            currentIndex = -1
        )

        val waypointsAtIndex0 = listOf(LatLng(1.0, 2.0))
        val stateAtIndex0 = routePlottingState.record(waypointsAtIndex0)

        assertEquals(0, stateAtIndex0.currentIndex)
        assertEquals(waypointsAtIndex0, stateAtIndex0.getCurrentState())
    }

    @Test
    fun testRecord_nonEmptyInitialState() {
        val waypointsAtIndex0 = listOf(LatLng(1.0, 2.0))
        val waypointsAtIndex1 = listOf(LatLng(3.0, 4.0))
        routePlottingState = RoutePlottingState(
            stateData = listOf(waypointsAtIndex0, waypointsAtIndex1),
            currentIndex = 1
        )

        val waypointsAtIndex2 = listOf(LatLng(5.0, 6.0))
        val stateAtIndex2 = routePlottingState.record(waypointsAtIndex2)

        assertEquals(2, stateAtIndex2.currentIndex)
        assertEquals(waypointsAtIndex2, stateAtIndex2.getCurrentState())
    }

    @Test
    fun testRecord_atMidIndex_overrideAllStatesFollowing() {
        val waypointsAtIndex0 = listOf(LatLng(1.0, 2.0))
        val waypointsAtIndex1 = listOf(LatLng(3.0, 4.0))
        routePlottingState = RoutePlottingState(
            stateData = listOf(waypointsAtIndex0, waypointsAtIndex1),
            currentIndex = 0
        )

        val waypointsAtIndex1Override = listOf(LatLng(5.0, 6.0))
        val stateAtIndex1 = routePlottingState.record(waypointsAtIndex1Override)

        assertEquals(1, stateAtIndex1.currentIndex)
        assertEquals(waypointsAtIndex1Override, stateAtIndex1.getCurrentState())
    }

    @Test
    fun testForward_atLastIndex() {
        routePlottingState = RoutePlottingState(
            stateData = listOf(listOf(LatLng(1.0, 2.0))),
            currentIndex = 0
        )
        val forwardResult = routePlottingState.forward()
        assertEquals(routePlottingState, forwardResult)
    }

    @Test
    fun testForward_atMidIndex() {
        val waypointsAtIndex0 = listOf(LatLng(1.0, 2.0))
        val waypointsAtIndex1 = listOf(LatLng(3.0, 4.0))
        routePlottingState = RoutePlottingState(
            stateData = listOf(waypointsAtIndex0, waypointsAtIndex1),
            currentIndex = 0
        )
        val forwardResult = routePlottingState.forward()
        assertEquals(1, forwardResult.currentIndex)
        assertEquals(waypointsAtIndex1, forwardResult.getCurrentState())
    }

    @Test
    fun testRewind_atFirstIndex() {
        routePlottingState = RoutePlottingState(
            stateData = listOf(listOf(LatLng(1.0, 2.0))),
            currentIndex = 0
        )
        val rewindResult = routePlottingState.rewind()
        assertEquals(routePlottingState, rewindResult)
    }

    @Test
    fun testRewind_atMidIndex() {
        val waypointsAtIndex0 = listOf(LatLng(1.0, 2.0))
        val waypointsAtIndex1 = listOf(LatLng(3.0, 4.0))
        routePlottingState = RoutePlottingState(
            stateData = listOf(waypointsAtIndex0, waypointsAtIndex1),
            currentIndex = 1
        )
        val rewindResult = routePlottingState.rewind()
        assertEquals(0, rewindResult.currentIndex)
        assertEquals(waypointsAtIndex0, rewindResult.getCurrentState())
    }
}
