package akio.apps.myrun.data.activity.impl.model

import akio.apps.myrun.data.activity.api.model.DataPointVersion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface LocalBaseActivityData {
    val id: String

    // user info
    val athleteInfo: LocalAthleteInfo

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
data class LocalAthleteInfo(
    @SerialName("userId")
    val userId: String,
    @SerialName("userName")
    val userName: String?,
    @SerialName("userAvatar")
    val userAvatar: String?,
)

@Serializable
data class LocalActivityData(
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
    override val athleteInfo: LocalAthleteInfo,
) : LocalBaseActivityData

@Serializable
sealed class LocalBaseActivity(
    // Set a name for serializer to differentiate with the extended class's field.
    @SerialName("LocalBaseActivity_activityData")
    open val activityData: LocalActivityData,
    @SerialName("LocalBaseActivity_version")
    open val version: Int
) : LocalBaseActivityData by activityData

@Serializable
data class LocalRunningActivity(
    override val activityData: LocalActivityData,

    // stats
    val pace: Double,
    val cadence: Int,
    override val version: Int = DataPointVersion.min().value
) : LocalBaseActivity(activityData, version)

@Serializable
data class LocalCyclingActivity(
    override val activityData: LocalActivityData,

    // stats
    val speed: Double,
    override val version: Int = DataPointVersion.min().value
) : LocalBaseActivity(activityData, version)
