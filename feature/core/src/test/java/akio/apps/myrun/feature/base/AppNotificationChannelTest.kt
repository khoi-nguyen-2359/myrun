package akio.apps.myrun.feature.base

import akio.apps.myrun.feature.core.AppNotificationChannel
import kotlin.test.assertFalse
import org.junit.Test

class AppNotificationChannelTest {
    @Test
    fun testUniqueId() {
        val idSet = mutableSetOf<String>()
        AppNotificationChannel.values().forEach {
            assertFalse(idSet.contains(it.id))
            idSet.add(it.id)
        }
    }

    @Test
    fun testUniqueBaseId() {
        val baseIdSet = mutableSetOf<Int>()
        AppNotificationChannel.values().forEach {
            assertFalse(baseIdSet.contains(it.baseNotificationId))
            baseIdSet.add(it.baseNotificationId)
        }
    }
}
