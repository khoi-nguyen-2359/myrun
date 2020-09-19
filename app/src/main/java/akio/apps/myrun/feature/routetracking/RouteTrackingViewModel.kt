package akio.apps.myrun.feature.routetracking

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import akio.apps.myrun.data.workout.dto.ActivityType
import akio.apps.myrun.data.routetracking.dto.RouteTrackingStatus
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.LiveData

abstract class RouteTrackingViewModel : BaseViewModel() {
    abstract val mapInitialLocation: LiveData<Event<Location>>
    abstract val trackingLocationBatch: LiveData<List<TrackingLocationEntity>>
    abstract val trackingStats: LiveData<RouteTrackingStats>
    abstract val trackingStatus: LiveData<@RouteTrackingStatus Int>
    abstract val saveWorkoutSuccess: LiveData<Event<Unit>>

    abstract fun requestDataUpdates()
    abstract fun cancelDataUpdates()

    abstract fun initialize()

    abstract fun saveWorkout(activityType: ActivityType, routeMapImage: Bitmap)
    abstract fun resumeDataUpdates()
}