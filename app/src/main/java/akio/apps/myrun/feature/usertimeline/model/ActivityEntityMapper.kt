package akio.apps.myrun.feature.usertimeline.model

import akio.apps.myrun.data.activity.ActivityEntity
import akio.apps.myrun.data.activity.CyclingActivityEntity
import akio.apps.myrun.data.activity.RunningActivityEntity
import javax.inject.Inject

class ActivityEntityMapper @Inject constructor() {
    fun map(activityId: String, entity: ActivityEntity): Activity {
        val activityData = entity.run {
            ActivityData(
                activityId,
                userId,
                userName,
                userAvatar,
                activityType,
                name,
                routeImage,
                startTime,
                endTime,
                duration,
                distance,
                encodedPolyline
            )
        }

        return when (entity) {
            is RunningActivityEntity -> RunningActivity(activityData, entity.pace)
            is CyclingActivityEntity -> CyclingActivity(activityData, entity.speed)
            else -> throw IllegalArgumentException("Unknown activity type")
        }
    }

    fun map(entity: ActivityEntity) = map(entity.id, entity)
}
