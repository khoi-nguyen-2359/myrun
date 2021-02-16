package akio.apps.myrun.feature.usertimeline.model

import akio.apps.myrun.data.activity.model.ActivityType

data class ActivityData(
    override val id: String,

    // activity info
    override val activityType: ActivityType,
    override val name: String,
    override val routeImage: String,
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long,
    override val distance: Double,
    override val encodedPolyline: String,

    // user info
    override val athleteInfo: Activity.AthleteInfo
) : Activity
