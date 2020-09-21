package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.data.activity.ActivityType
import android.graphics.Bitmap

interface SaveRouteTrackingActivityUsecase {
    suspend fun saveCurrentActivity(activityType: ActivityType, routeMapImage: Bitmap)
}