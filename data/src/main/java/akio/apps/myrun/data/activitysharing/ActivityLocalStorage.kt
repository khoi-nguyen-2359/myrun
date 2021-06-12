package akio.apps.myrun.data.activitysharing

import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import akio.apps.myrun.data.activitysharing.model.ActivityStorageDataOutput
import android.graphics.Bitmap

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

    suspend fun loadActivityData(activityId: String): ActivityStorageDataOutput

    suspend fun getStoredActivityIds(): List<String>

    suspend fun deleteActivityData(activityId: String)
}
