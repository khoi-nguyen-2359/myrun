package akio.apps.myrun.data.activity.impl.model

import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.activity.impl.FirebaseActivityRepository
import android.net.Uri
import javax.inject.Inject

class FirestoreActivityMapper @Inject constructor() {
    fun map(input: FirestoreActivity): ActivityModel {
        val activityType = when (FirestoreActivityType.fromId(input.activityType)) {
            FirestoreActivityType.Running -> ActivityType.Running
            FirestoreActivityType.Cycling -> ActivityType.Cycling
            else -> ActivityType.Unknown
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
                ActivityModel.AthleteInfo(
                    athleteInfo.userId,
                    athleteInfo.userName,
                    athleteInfo.userAvatar
                )
            )
        }

        return input.run {
            runningData?.run {
                // TODO: Replace this with stored pace field when migration are done on firestore.
                val fixedPace = (duration / 60000.0) / (distance / 1000)
                RunningActivityModel(activityData, fixedPace, cadence)
            }
                ?: cyclingData?.run {
                    CyclingActivityModel(activityData, speed)
                }
        }
            ?: throw IllegalArgumentException("Got invalid activity type while parsing")
    }

    fun mapRev(input: ActivityModel, createdId: String, uploadedImageUri: Uri): FirestoreActivity {
        val runData: FirestoreRunningData? = (input as? RunningActivityModel)
            ?.run { FirestoreRunningData(pace, cadence) }
        val cyclingData: FirestoreCyclingData? = (input as? CyclingActivityModel)
            ?.run { FirestoreCyclingData(speed) }
        val activityType = when (input.activityType) {
            ActivityType.Running -> FirestoreActivityType.Running
            ActivityType.Cycling -> FirestoreActivityType.Cycling
            else -> FirestoreActivityType.Unknown
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
                version = FirebaseActivityRepository.ACTIVITY_DATA_VERSION
            )
        }
    }
}
