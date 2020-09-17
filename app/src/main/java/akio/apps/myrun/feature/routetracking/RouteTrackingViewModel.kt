package akio.apps.myrun.feature.routetracking

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import android.location.Location
import androidx.lifecycle.LiveData

abstract class RouteTrackingViewModel : BaseViewModel() {
    abstract val mapInitialLocation: LiveData<Event<Location>>
    abstract val trackingLocationBatch: LiveData<List<TrackingLocationEntity>>
    abstract fun requestMapInitialLocation()
    abstract fun requestRouteTrackingLocationUpdate(skip: Int)
}