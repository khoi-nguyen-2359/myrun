package akio.apps.myrun.feature.usertimeline.model

import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import javax.inject.Inject

class ActivityModelMapper @Inject constructor() {
    fun map(activityId: String, model: ActivityModel): Activity {
        val activityData = model.run {
            ActivityData(
                activityId,
                activityType,
                name,
                routeImage,
                placeIdentifier,
                startTime,
                endTime,
                duration,
                distance,
                encodedPolyline,
                mapAthleteInfo(athleteInfo)
            )
        }

        return when (model) {
            is RunningActivityModel -> RunningActivity(activityData, model.pace, model.cadence)
            is CyclingActivityModel -> CyclingActivity(activityData, model.speed)
            else -> throw IllegalArgumentException("Unknown activity type")
        }
    }

    fun map(model: ActivityModel) = map(model.id, model)

    private fun mapAthleteInfo(athleteInfoModel: ActivityModel.AthleteInfo) =
        with(athleteInfoModel) {
            Activity.AthleteInfo(userId, userName, userAvatar)
        }

    private fun mapAthleteInfoRev(athleteInfo: Activity.AthleteInfo) = with(athleteInfo) {
        ActivityModel.AthleteInfo(userId, userName, userAvatar)
    }

    fun mapRev(activity: Activity): ActivityModel {
        val activityData = with(activity) {
            ActivityDataModel(
                id,
                activityType,
                name,
                routeImage,
                placeIdentifier,
                startTime,
                endTime,
                duration,
                distance,
                encodedPolyline,
                mapAthleteInfoRev(athleteInfo)
            )
        }

        return when (activity) {
            is RunningActivity -> RunningActivityModel(
                activityData,
                activity.pace,
                activity.cadence
            )
            is CyclingActivity -> CyclingActivityModel(activityData, activity.speed)
            else -> throw IllegalArgumentException("Unknown activity type")
        }
    }
}
