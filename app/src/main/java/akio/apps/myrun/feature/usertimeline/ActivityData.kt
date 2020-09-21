package akio.apps.myrun.feature.usertimeline

import akio.apps.myrun.data.activity.ActivityType

data class ActivityData(
    override val id: String,
    override val activityType: ActivityType,
    override val startTime: Long,
    override val endTime: Long,
    override val duration: Long
) : Activity