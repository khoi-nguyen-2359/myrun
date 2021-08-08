package akio.apps._base

import akio.apps.myrun.feature.configuration.impl.ConfiguratorActivity
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfiguratorActivityTest {
    /**
     * This activity are resolved by class name so use this test to make sure it's unchanged.
     */
    @Test
    fun testConsistentActivityName() {
        assertEquals(
            "akio.apps.myrun.feature.configuration.impl.ConfiguratorActivity",
            akio.apps.myrun.feature.configuration.impl.ConfiguratorActivity::class.qualifiedName
        )
    }
}
