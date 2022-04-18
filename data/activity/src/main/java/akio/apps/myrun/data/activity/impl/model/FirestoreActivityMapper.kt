package akio.apps.myrun.data.activity.impl.model

import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import android.net.Uri
import javax.inject.Inject

class FirestoreActivityMapper @Inject constructor() {
    fun map(input: FirestoreActivity): BaseActivityModel {
        val activityType = when (FirestoreActivityType.fromId(input.activityType)) {
            FirestoreActivityType.Running -> ActivityType.Running
            FirestoreActivityType.Cycling -> ActivityType.Cycling
            FirestoreActivityType.Unknown -> ActivityType.Unknown
        }
        val activityData = with(input) {
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
                AthleteInfo(athleteInfo.userId, athleteInfo.userName, athleteInfo.userAvatar)
            )
        }

        return input.runningData?.run { RunningActivityModel(activityData, pace, cadence) }
            ?: input.cyclingData?.run { CyclingActivityModel(activityData, speed) }
            ?: throw IllegalArgumentException("Invalid activity type.")
    }

    fun mapRev(
        input: BaseActivityModel,
        createdId: String,
        uploadedImageUri: Uri,
    ): FirestoreActivity {
        val runData: FirestoreRunningData? = (input as? RunningActivityModel)
            ?.run { FirestoreRunningData(pace, cadence) }
        val cyclingData: FirestoreCyclingData? = (input as? CyclingActivityModel)
            ?.run { FirestoreCyclingData(speed) }
        val activityType = when (input.activityType) {
            ActivityType.Running -> FirestoreActivityType.Running
            ActivityType.Cycling -> FirestoreActivityType.Cycling
            ActivityType.Unknown -> FirestoreActivityType.Unknown
        }

        return with(input) {
            FirestoreActivity(
                id = createdId,
                activityType = activityType.id,
                name = name,
                routeImage = uploadedImageUri.toString(),
                placeIdentifier = input.placeIdentifier,
                startTime = startTime,
                endTime = endTime,
                duration = duration,
                distance = distance,
                encodedPolyline = encodedPolyline,
                runningData = runData,
                cyclingData = cyclingData,
                athleteInfo = FirestoreActivityAthleteInfo(
                    athleteInfo.userId,
                    athleteInfo.userName,
                    athleteInfo.userAvatar
                ),
            )
        }
    }
}
