package akio.apps._base

import akio.apps.myrun.configurator.ConfiguratorActivity
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfiguratorActivityTest {
    /**
     * This activity are resolved by class name so use this test to make sure it's unchanged.
     */
    @Test
    fun testConsistentActivityName() {
        assertEquals(
            "akio.apps.myrun.configurator.ConfiguratorActivity",
            ConfiguratorActivity::class.qualifiedName
        )
    }
}
