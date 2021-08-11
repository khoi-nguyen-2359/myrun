package akio.apps.myrun.feature.configurator

import org.junit.Assert.assertEquals
import org.junit.Test

class ConfiguratorGateTest {
    /**
     * This activity are resolved by class name so use this test to make sure it's unchanged.
     */
    @Test
    fun testConsistentActivityName() {
        assertEquals(
            "akio.apps.myrun.feature.configurator.ConfiguratorActivity",
            ConfiguratorActivity::class.qualifiedName
        )
    }
}
