package akio.apps.myrun.data.activitysharing

import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import akio.apps.myrun.data.activitysharing.model.ActivityStorageData
import akio.apps.myrun.data.activitysharing.model.ActivitySyncData
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface ActivityLocalStorage {
    suspend fun storeActivityData(
        activity: ActivityModel,
        locations: List<ActivityLocation>,
        routeBitmap: Bitmap
    )

    suspend fun storeActivitySyncData(
        activityModel: ActivityModel,
        activityLocations: List<ActivityLocation>
    )

    suspend fun loadAllActivityStorageDataFlow(): Flow<ActivityStorageData>

    suspend fun deleteActivityData(activityId: String)

    fun getActivityStorageDataCount(): Flow<Int>
    suspend fun setActivityStorageDataCount(count: Int)

    /**
     * Returns all data that are pending to be synced to Strava.
     */
    fun loadAllActivitySyncDataFlow(): Flow<ActivitySyncData>
    fun deleteActivitySyncData(activityId: String)
}
