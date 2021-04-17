package akio.apps.myrun.domain

import org.junit.Assert.assertNull
import org.junit.Test

class PerformanceUnitTest {
    @Test
    fun testObjectsHaveUniqueIds() {
        val mapUniqueCheck = mutableMapOf<String, Boolean>()
        PerformanceUnit::class.nestedClasses
            .mapNotNull { (it.objectInstance as? PerformanceUnit<*>)?.id }
            .forEach { objectId ->
                assertNull(mapUniqueCheck[objectId])
                mapUniqueCheck[objectId] = true
            }
    }
}
