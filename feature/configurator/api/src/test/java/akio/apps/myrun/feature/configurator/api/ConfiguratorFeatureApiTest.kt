package akio.apps.myrun.feature.configurator.api

import akio.apps.myrun.feature.configuration.impl.ConfiguratorActivity
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfiguratorFeatureApiTest {
    /**
     * This activity are resolved by class name so use this test to make sure it's unchanged.
     */
    @Test
    fun testConsistentActivityName() {
        assertEquals(
            "akio.apps.myrun.feature.configuration.impl.ConfiguratorActivity",
            ConfiguratorActivity::class.qualifiedName
        )
    }
}
