package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.data.place.PlaceEntity
import android.graphics.Bitmap

interface SaveRouteTrackingActivityUsecase {
    suspend fun saveCurrentActivity(activityType: ActivityType, routeMapImage: Bitmap)
}