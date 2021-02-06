package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.feature.usertimeline.model.Activity
import android.graphics.Bitmap

interface SaveRouteTrackingActivityUsecase {
    suspend fun saveCurrentActivity(activityType: ActivityType, routeMapImage: Bitmap): Activity
}
