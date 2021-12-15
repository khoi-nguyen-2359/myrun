package akio.apps.myrun.data.activitysharing.entity

import akio.apps.myrun.data.activity.impl.model.AthleteInfo
import akio.apps.myrun.data.activity.impl.model.RunningTrackingActivityInfo
import akio.apps.myrun.data.activity.impl.model.TrackingActivityInfoData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalSerializationApi
class TrackingActivityInfoTest {
    @Test
    fun testSerializationOnInstanceDelegates() {
        val trackingInfoData = TrackingActivityInfoData(
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
            AthleteInfo("userId", "userName", "userAvatar")
        )
        val info = RunningTrackingActivityInfo(
            activityData = trackingInfoData,
            pace = 4.0,
            cadence = 0
        )
        val infoBytes = ProtoBuf.encodeToByteArray(info)
        val infoObject = ProtoBuf.decodeFromByteArray<RunningTrackingActivityInfo>(infoBytes)
        assertEquals(infoObject, info)
    }
}
