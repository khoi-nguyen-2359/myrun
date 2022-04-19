package akio.apps.myrun.data.location.api

import akio.apps.myrun.data.location.api.model.LatLng
import kotlin.test.assertEquals
import org.junit.Test

class WaypointReducerTest {

    @Test
    fun reduce_quantityIsLarger() {
        val reducer = WaypointReducer()
        val originalWaypoints = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0)
        )
        val reduced = reducer.reduce(originalWaypoints, 3)
        assertEquals(3, reduced.size)
    }

    @Test
    fun reduce_quantityIsEqual() {
        val reducer = WaypointReducer()
        val originalWaypoints = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0)
        )
        val reduced = reducer.reduce(originalWaypoints, 5)
        assertEquals(5, reduced.size)
    }

    @Test
    fun reduce_quantityIsSmaller() {
        val reducer = WaypointReducer()
        val originalWaypoints = listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0),
            LatLng(0.0, 0.0)
        )
        val reduced = reducer.reduce(originalWaypoints, 7)
        assertEquals(5, reduced.size)
    }
}
