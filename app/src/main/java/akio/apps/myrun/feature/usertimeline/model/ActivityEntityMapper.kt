package akio.apps.myrun.feature.usertimeline.model

import akio.apps.myrun.data.activity.RunningActivityEntity
import akio.apps.myrun.data.activity.ActivityEntity
import akio.apps.myrun.data.activity.CyclingActivityEntity
import javax.inject.Inject

class ActivityEntityMapper @Inject constructor() {
    fun map(entity: ActivityEntity): Activity {
        val activityData = entity.run {
            ActivityData(id, userId, userName, userAvatar, activityType, name, routeImage, startTime, endTime, duration, distance, encodedPolyline)
        }

        return when (entity) {
            is RunningActivityEntity -> RunningActivity(activityData, entity.pace)
            is CyclingActivityEntity -> CyclingActivity(activityData, entity.speed)
            else -> throw IllegalArgumentException("Unknown activity type")
        }
    }
}