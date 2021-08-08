package akio.apps.myrun.data.activity.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface TrackingActivityInfo {
    val id: String

    // user info
    val athleteInfo: AthleteInfo

    // info
    val activityType: String
    val name: String
    val routeImage: String
    val placeIdentifier: String?

    // stats
    val startTime: Long
    val endTime: Long
    val duration: Long
    val distance: Double

    // data points
    val encodedPolyline: String
}

@Serializable
data class AthleteInfo(
    @SerialName("userId")
    val userId: String,
    @SerialName("userName")
    val userName: String?,
    @SerialName("userAvatar")
    val userAvatar: String?
)

@Serializable
data class TrackingActivityInfoData(
    override val id: String,

    // info
    override val activityType: String,
    override val name: String,
    override val routeImage: String,
    override val placeIdentifier: String?,

    // stats
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long,
    override val distance: Double,
    override val encodedPolyline: String,

    // user info
    override val athleteInfo: AthleteInfo
) : TrackingActivityInfo

@Serializable
data class RunningTrackingActivityInfo(
    val activityData: TrackingActivityInfoData,

    // stats
    val pace: Double,
    val cadence: Int
) : TrackingActivityInfo by activityData

@Serializable
data class CyclingTrackingActivityInfo(
    val activityData: TrackingActivityInfoData,

    // stats
    val speed: Double,
) : TrackingActivityInfo by activityData
