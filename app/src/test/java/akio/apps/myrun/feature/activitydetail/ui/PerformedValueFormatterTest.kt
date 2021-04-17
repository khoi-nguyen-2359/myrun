package akio.apps.myrun.feature.activitydetail.ui

import org.junit.Assert.assertNull
import org.junit.Test

class PerformedValueFormatterTest {
    @Test
    fun testEnumsHaveUniqueIds() {
        val mapUniqueCheck = mutableMapOf<String, Boolean>()
        PerformedValueFormatter.values()
            .map { it.id }
            .forEach { enumId ->
                assertNull(mapUniqueCheck[enumId])
                mapUniqueCheck[enumId] = true
            }
    }
}
