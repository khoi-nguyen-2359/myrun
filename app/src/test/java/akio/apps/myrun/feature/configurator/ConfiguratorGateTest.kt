package akio.apps.myrun.feature.configurator

import akio.apps.myrun.feature.configurator.ConfiguratorGate.CONFIGURATOR_ACTIVITY_NAME
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfiguratorGateTest {
    /**
     * This activity are resolved by class name so use this test to make sure it's unchanged.
     */
    @Test
    fun testConsistentActivityName() {
        assertEquals(
            CONFIGURATOR_ACTIVITY_NAME,
            ConfiguratorActivity::class.qualifiedName
        )
    }
}
