package akio.apps.myrun.feature.routetracking

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import akio.apps.myrun.data.workout.dto.ActivityType
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingStatus
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.LiveData

abstract class RouteTrackingViewModel : BaseViewModel() {
    abstract val mapInitialLocation: LiveData<Event<Location>>
    abstract val trackingLocationBatch: LiveData<List<TrackingLocationEntity>>
    abstract val trackingStats: LiveData<RouteTrackingStats>
    abstract val trackingStatus: LiveData<RouteTrackingStatus>
    abstract val saveWorkoutResult: LiveData<Event<Unit>>

    abstract fun requestDataUpdates()
    abstract fun cancelDataUpdates()

    abstract fun requestMapInitialLocation()

    abstract fun startRouteTracking()
    abstract fun pauseRouteTracking()
    abstract fun resumeRouteTracking()
    abstract fun stopRouteTracking()
    abstract fun restoreTrackingStatus()
    abstract fun saveWorkout(activityType: ActivityType, routeMapImage: Bitmap)
}