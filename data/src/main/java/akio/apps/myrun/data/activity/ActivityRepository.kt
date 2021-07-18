package akio.apps.myrun.data.activity

import akio.apps.myrun.data.activity.model.ActivityLocation
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.fitness.DataPoint
import java.io.File

interface ActivityRepository {
    suspend fun getActivitiesByStartTime(
        userIds: List<String>,
        startAfterTime: Long,
        limit: Int
    ): List<ActivityModel>

    suspend fun saveActivity(
        activity: ActivityModel,
        routeBitmapFile: File,
        speedDataPoints: List<DataPoint<Float>>,
        stepCadenceDataPoints: List<DataPoint<Int>>?,
        locationDataPoints: List<ActivityLocation>
    ): String

    suspend fun getActivity(activityId: String): ActivityModel?
    suspend fun getActivityLocationDataPoints(activityId: String):
        List<ActivityLocation>
}
