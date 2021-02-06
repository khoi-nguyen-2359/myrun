package akio.apps.myrun.feature.usertimeline.model

import akio.apps.myrun.data.activity.model.ActivityDataModel
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.CyclingActivityModel
import akio.apps.myrun.data.activity.model.RunningActivityModel
import javax.inject.Inject

class ActivityEntityMapper @Inject constructor() {
    fun map(activityId: String, model: ActivityModel): Activity {
        val activityData = model.run {
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

        return when (model) {
            is RunningActivityModel -> RunningActivity(activityData, model.pace)
            is CyclingActivityModel -> CyclingActivity(activityData, model.speed)
            else -> throw IllegalArgumentException("Unknown activity type")
        }
    }

    fun map(model: ActivityModel) = map(model.id, model)

    fun mapRev(activity: Activity): ActivityModel {
        val activityData = with(activity) {
            ActivityDataModel(
                id,
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

        return when (activity) {
            is RunningActivity -> RunningActivityModel(activityData, activity.pace)
            is CyclingActivity -> CyclingActivityModel(activityData, activity.speed)
            else -> throw IllegalArgumentException("Unknown activity type")
        }
    }
}
