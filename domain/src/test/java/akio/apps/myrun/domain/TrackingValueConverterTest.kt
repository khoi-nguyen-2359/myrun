package akio.apps.myrun.domain

import org.junit.Assert.assertNull
import org.junit.Test

class TrackingValueConverterTest {
    @Test
    fun testObjectsHaveUniqueIds() {
        val mapUniqueCheck = mutableMapOf<String, Boolean>()
        TrackingValueConverter::class.nestedClasses
            .mapNotNull { (it.objectInstance as? TrackingValueConverter<*>)?.id }
            .forEach { objectId ->
                assertNull(mapUniqueCheck[objectId])
                mapUniqueCheck[objectId] = true
            }
    }
}
