package akio.apps.myrun.data.activity

import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import android.graphics.Bitmap
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
        speedDataPoints: List<SingleDataPoint<Float>>,
        stepCadenceDataPoints: List<SingleDataPoint<Int>>?,
        locationDataPoints: List<SingleDataPoint<LocationEntity>>
    ): String

    suspend fun getActivity(activityId: String): ActivityModel?
    suspend fun getActivityLocationDataPoints(activityId: String):
        List<SingleDataPoint<LocationEntity>>

    fun generateActivityId(userId: String): String
}
