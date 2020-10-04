package akio.apps.myrun.data.activity.entity

import akio.apps.myrun.data.activity.*
import android.net.Uri
import javax.inject.Inject

class FirestoreActivityMapper @Inject constructor() {
    fun map(input: FirestoreActivity): ActivityEntity {
        val activityType = when (input.activityType) {
            FirestoreActivityType.Running -> ActivityType.Running
            FirestoreActivityType.Cycling -> ActivityType.Cycling
            else -> ActivityType.Unknown
        }

        val activityData = input.run {
            ActivityDataEntity(
                id, userId, activityType, name, routeImage, startTime, endTime, duration, distance, encodedPolyline
            )
        }

        return input.run {
            runningData?.run {
                RunningActivityEntity(activityData = activityData, pace = pace)
            } ?: cyclingData?.run {
                CyclingActivityEntity(activityData = activityData, speed = speed)
            }
        }
            ?: throw IllegalArgumentException("Got invalid activity type while parsing")
    }

    fun mapRev(input: ActivityEntity, createdId: String, uploadedUri: Uri): FirestoreActivity {
        val runData: FirestoreRunningData? = (input as? RunningActivityEntity)
            ?.run { FirestoreRunningData(pace) }

        val cyclingData: FirestoreCyclingData? = (input as? CyclingActivityEntity)
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