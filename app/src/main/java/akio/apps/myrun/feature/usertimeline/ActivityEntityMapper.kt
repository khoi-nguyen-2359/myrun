package akio.apps.myrun.feature.usertimeline

import akio.apps.myrun.data.activity.RunningActivityEntity
import akio.apps.myrun.data.activity.ActivityEntity
import javax.inject.Inject

class ActivityEntityMapper @Inject constructor() {
    fun map(entity: ActivityEntity): Activity {
        val activityData = entity.run {
            ActivityData(id, activityType, startTime, endTime, duration)
        }

        return if (entity is RunningActivityEntity) {
            RunningActivity(activityData, entity.routePhoto, entity.averagePace, entity.distance, entity.encodedPolyline)
        } else throw IllegalArgumentException("Unknown activity type")
    }
}