package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.feature.activitydetail.TrackingValueFormatter
import org.junit.Assert.assertNull
import org.junit.Test

class TrackingValueFormatterTest {
    @Test
    fun testEnumsHaveUniqueIds() {
        val mapUniqueCheck = mutableMapOf<String, Boolean>()
        TrackingValueFormatter::class.sealedSubclasses.mapNotNull { it.objectInstance }
            .map { it.id }
            .forEach { enumId ->
                assertNull(mapUniqueCheck[enumId])
                mapUniqueCheck[enumId] = true
            }
    }
}
