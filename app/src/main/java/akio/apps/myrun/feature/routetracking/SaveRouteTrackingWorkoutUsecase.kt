package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.data.workout.dto.ActivityType
import android.graphics.Bitmap

interface SaveRouteTrackingWorkoutUsecase {
    suspend fun saveCurrentWorkout(activityType: ActivityType, routeMapImage: Bitmap)
}