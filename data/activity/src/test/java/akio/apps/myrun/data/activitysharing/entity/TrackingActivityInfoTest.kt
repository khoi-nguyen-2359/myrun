package akio.apps.myrun.data.activitysharing.entity

import akio.apps.myrun.data.activity.impl.model.LocalActivityData
import akio.apps.myrun.data.activity.impl.model.LocalAthleteInfo
import akio.apps.myrun.data.activity.impl.model.LocalBaseActivity
import akio.apps.myrun.data.activity.impl.model.LocalRunningActivity
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalSerializationApi
class TrackingActivityInfoTest {
    @Test
    fun testSerializationOnInstanceDelegates() {
        // need the explicit type declaration for LocalRunningActivity
        val info: LocalRunningActivity = createSampleLocalActivity()
        val infoBytes = ProtoBuf.encodeToByteArray(info)
        val infoObject = ProtoBuf.decodeFromByteArray<LocalRunningActivity>(infoBytes)
        assertEquals(infoObject, info)
    }

    @Test
    fun testSerializationOnPolymorphicSerializer() {
        // need the explicit type declaration for LocalBaseActivity
        val info: LocalBaseActivity = createSampleLocalActivity()
        val protoBuf = ProtoBuf {
            val module = SerializersModule {
                polymorphic(
                    LocalBaseActivity::class,
                    LocalRunningActivity::class,
                    LocalRunningActivity.serializer()
                )
            }
            serializersModule = module
        }
        val infoBytes = protoBuf.encodeToByteArray(info)
        val infoObject = protoBuf.decodeFromByteArray<LocalBaseActivity>(infoBytes)
        assertEquals(infoObject, info)
    }

    private fun createSampleLocalActivity(): LocalRunningActivity {
        val trackingInfoData = LocalActivityData(
            "id",
            "activityType",
            "name",
            "routeImage",
            "placeIdentifier",
            startTime = 1000L,
            endTime = 1000L,
            duration = 1000L,
            distance = 10.0,
            "encodedPolyline",
            LocalAthleteInfo("userId", "userName", "userAvatar")
        )
        return LocalRunningActivity(
            activityData = trackingInfoData,
            pace = 4.0,
            cadence = 0
        )
    }
}
