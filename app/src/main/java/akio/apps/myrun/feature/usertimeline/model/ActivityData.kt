package akio.apps.myrun.feature.usertimeline.model

import akio.apps.myrun.data.activity.ActivityType

data class ActivityData(
    override val id: String,
    override val activityType: ActivityType,
    override val userId: String,
    override val name: String,
    override val routeImage: String,
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long,
    override val distance: Double,
    override val encodedPolyline: String
) : Activity