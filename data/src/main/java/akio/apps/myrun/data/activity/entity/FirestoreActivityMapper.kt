package akio.apps.myrun.data.activity.entity

import akio.apps.myrun.data.activity.model.ActivityDataModel
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activity.model.CyclingActivityModel
import akio.apps.myrun.data.activity.model.RunningActivityModel
import android.net.Uri
import javax.inject.Inject

class FirestoreActivityMapper @Inject constructor() {
    fun map(input: FirestoreActivity): ActivityModel {
        val activityType = when (input.activityType) {
            FirestoreActivityType.Running -> ActivityType.Running
            FirestoreActivityType.Cycling -> ActivityType.Cycling
            else -> ActivityType.Unknown
        }

        val activityData = input.run {
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

        return input.run {
            runningData?.run {
                RunningActivityModel(activityData = activityData, pace = pace)
            } ?: cyclingData?.run {
                CyclingActivityModel(activityData = activityData, speed = speed)
            }
        }
            ?: throw IllegalArgumentException("Got invalid activity type while parsing")
    }

    fun mapRev(input: ActivityModel, createdId: String, uploadedUri: Uri): FirestoreActivity {
        val runData: FirestoreRunningData? = (input as? RunningActivityModel)
            ?.run { FirestoreRunningData(pace) }

        val cyclingData: FirestoreCyclingData? = (input as? CyclingActivityModel)
            ?.run { FirestoreCyclingData(speed) }

        val activityType = when (input.activityType) {
            ActivityType.Running -> FirestoreActivityType.Running
            ActivityType.Cycling -> FirestoreActivityType.Cycling
            else -> FirestoreActivityType.Unknown
        }

        return input.run {
            FirestoreActivity(
                id = createdId,
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                activityType = activityType,
                name = name,
                routeImage = uploadedUri.toString(),
                startTime = startTime,
                endTime = endTime,
                duration = duration,
                distance = distance,
                encodedPolyline = encodedPolyline,
                runningData = runData,
                cyclingData = cyclingData
            )
        }
    }
}
