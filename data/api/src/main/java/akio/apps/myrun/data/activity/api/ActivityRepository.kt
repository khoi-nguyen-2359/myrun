package akio.apps.myrun.data.activity.api

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.DataPoint
import akio.apps.myrun.data.common.Resource
import java.io.File

interface ActivityRepository {
    suspend fun getActivitiesByStartTime(
        fixUserId: String,
        userIds: List<String>,
        startAfterTime: Long,
        limit: Int,
    ): List<ActivityModel>

    /**
     * [stepCadenceDataPoints] and [runSplits] are for Running activity.
     */
    suspend fun saveActivity(
        activity: ActivityModel,
        routeBitmapFile: File,
        speedDataPoints: List<DataPoint<Float>>,
        locationDataPoints: List<ActivityLocation>,
        stepCadenceDataPoints: List<DataPoint<Int>>?,
    ): String

    suspend fun getActivity(activityId: String): ActivityModel?

    /**
     * Get activity data at given [activityId]. Returns null data if the id is not exist or in error
     * case.
     */
    suspend fun getActivityResource(activityId: String): Resource<ActivityModel?>
    suspend fun getActivityLocationDataPoints(activityId: String):
        List<ActivityLocation>

    /**
     * Get activities that have start time in given time range. Sorting order is descending.
     */
    suspend fun getActivitiesInTimeRange(
        userId: String,
        startTime: Long,
        endTime: Long,
    ): List<ActivityModel>
}
