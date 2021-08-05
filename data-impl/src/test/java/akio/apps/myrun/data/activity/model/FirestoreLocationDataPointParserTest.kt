package akio.apps.myrun.data.activity.model

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
                "activityId",
                time = 0,
                latitude = 1.0,
                longitude = 2.0,
                altitude = 32.0
            ),
            ActivityLocation(
                "activityId",
                time = 4,
                latitude = 5.0,
                longitude = 6.0,
                altitude = 71.0
            ),
            ActivityLocation(
                "activityId",
                time = 8,
                latitude = 9.0,
                longitude = 10.0,
                altitude = 13.0
            )
        )
        val result = firestoreLocationDataPointParser.flatten(locations)
        assertEquals(listOf(0.0, 1.0, 2.0, 32.0, 4.0, 5.0, 6.0, 71.0, 8.0, 9.0, 10.0, 13.0), result)
    }

    @Test
    fun testBuild() {
        val dataPoints = listOf(0.0, 1.0, 2.0, 32.0, 4.0, 5.0, 6.0, 71.0, 8.0, 9.0, 10.0, 13.0)
        val locations = firestoreLocationDataPointParser.build("activityId", dataPoints)
        assertEquals(
            listOf(
                ActivityLocation(
                    "activityId",
                    time = 0,
                    latitude = 1.0,
                    longitude = 2.0,
                    altitude = 32.0
                ),
                ActivityLocation(
                    "activityId",
                    time = 4,
                    latitude = 5.0,
                    longitude = 6.0,
                    altitude = 71.0
                ),
                ActivityLocation(
                    "activityId",
                    time = 8,
                    latitude = 9.0,
                    longitude = 10.0,
                    altitude = 13.0
                )
            ),
            locations
        )
    }
}
