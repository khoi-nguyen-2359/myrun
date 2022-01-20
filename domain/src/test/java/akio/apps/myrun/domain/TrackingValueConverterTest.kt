package akio.apps.myrun.domain

import akio.apps.myrun.domain.common.TrackingValueConverter
import org.junit.Assert.assertNull
import org.junit.Test

class TrackingValueConverterTest {
    @Test
    fun testObjectsHaveUniqueIds() {
        val mapUniqueCheck = mutableMapOf<String, Boolean>()
        TrackingValueConverter::class.sealedSubclasses
            .mapNotNull { it.objectInstance }
            .forEach { objectId ->
                assertNull(mapUniqueCheck[objectId.id])
                mapUniqueCheck[objectId.id] = true
            }
    }
}
