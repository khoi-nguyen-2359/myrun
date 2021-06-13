package akio.apps.myrun.data.activitysharing

import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import akio.apps.myrun.data.activitysharing.model.ActivityStorageDataOutput
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

    suspend fun loadAllActivityStorageDataFlow(): Flow<ActivityStorageDataOutput>

    suspend fun deleteActivityData(activityId: String)
}
