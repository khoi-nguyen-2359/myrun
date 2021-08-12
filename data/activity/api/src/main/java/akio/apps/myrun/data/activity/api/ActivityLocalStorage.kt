package akio.apps.myrun.data.activity.api

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.ActivityStorageData
import akio.apps.myrun.data.activity.api.model.ActivitySyncData
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

    fun getActivityStorageDataCountFlow(): Flow<Int>

    /**
     * Returns all data that are pending to be synced to Strava.
     */
    fun loadAllActivitySyncDataFlow(): Flow<ActivitySyncData>
    fun deleteActivitySyncData(activityId: String)

    suspend fun clearAll()
}