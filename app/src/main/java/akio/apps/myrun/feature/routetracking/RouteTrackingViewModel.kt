package akio.apps.myrun.feature.routetracking

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import android.location.Location
import androidx.lifecycle.LiveData

abstract class RouteTrackingViewModel : BaseViewModel() {
    abstract val mapInitialLocation: LiveData<Event<Location>>
    abstract val trackingLocationBatch: LiveData<List<TrackingLocationEntity>>
    abstract val trackingStats: LiveData<RouteTrackingStats>

    abstract fun startTrackingStatsUpdates()
    abstract fun resumeTrackingStatsUpdates()
    abstract fun stopTrackingStatsUpdates()

    abstract fun requestMapInitialLocation()
    abstract fun requestRouteTrackingLocationUpdate(skip: Int)
}