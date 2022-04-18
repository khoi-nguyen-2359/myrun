package akio.apps.myrun.data.activity.api.locationparser

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LocationDataPointParserV1Test {

    private lateinit var locationDataPointParserV1: LocationDataPointParserV1

    @Before
    fun setup() {
        locationDataPointParserV1 = LocationDataPointParserV1()
    }

    @Test
    fun testFlatten() {
        val locations = listOf(
            ActivityLocation(
                elapsedTime = 0,
                latitude = 1.0,
                longitude = 2.0,
                altitude = 32.0,
                speed = 0.0
            ),
            ActivityLocation(
                elapsedTime = 4,
                latitude = 5.0,
                longitude = 6.0,
                altitude = 71.0,
                speed = 0.0
            ),
            ActivityLocation(
                elapsedTime = 8,
                latitude = 9.0,
                longitude = 10.0,
                altitude = 13.0,
                speed = 0.0
            )
        )
        val result = locationDataPointParserV1.flatten(locations)
        assertEquals(listOf(0.0, 1.0, 2.0, 32.0, 4.0, 5.0, 6.0, 71.0, 8.0, 9.0, 10.0, 13.0), result)
    }

    @Test
    fun testBuild() {
        val dataPoints = listOf(0.0, 1.0, 2.0, 32.0, 4.0, 5.0, 6.0, 71.0, 8.0, 9.0, 10.0, 13.0)
        val locations = locationDataPointParserV1.build(dataPoints)
        assertEquals(
            listOf(
                ActivityLocation(
                    elapsedTime = 0,
                    latitude = 1.0,
                    longitude = 2.0,
                    altitude = 32.0,
                    speed = 0.0
                ),
                ActivityLocation(
                    elapsedTime = 4,
                    latitude = 5.0,
                    longitude = 6.0,
                    altitude = 71.0,
                    speed = 0.0
                ),
                ActivityLocation(
                    elapsedTime = 8,
                    latitude = 9.0,
                    longitude = 10.0,
                    altitude = 13.0,
                    speed = 0.0
                )
            ),
            locations
        )
    }
}
