package akio.apps.myrun.data.activity.model

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.impl.model.FirestoreLocationDataPointParser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FirestoreLocationDataPointParserTest {

    private lateinit var firestoreLocationDataPointParser: FirestoreLocationDataPointParser

    @Before
    fun setup() {
        firestoreLocationDataPointParser = FirestoreLocationDataPointParser()
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
        val result = firestoreLocationDataPointParser.flatten(locations)
        assertEquals(listOf(0.0, 1.0, 2.0, 32.0, 4.0, 5.0, 6.0, 71.0, 8.0, 9.0, 10.0, 13.0), result)
    }

    @Test
    fun testBuild() {
        val dataPoints = listOf(0.0, 1.0, 2.0, 32.0, 4.0, 5.0, 6.0, 71.0, 8.0, 9.0, 10.0, 13.0)
        val locations = firestoreLocationDataPointParser.build(dataPoints)
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