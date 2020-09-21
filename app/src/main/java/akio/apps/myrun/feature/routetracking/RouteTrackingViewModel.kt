package akio.apps.myrun.feature.routetracking

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.feature.routetracking.model.LatLng
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import android.graphics.Bitmap
import androidx.lifecycle.LiveData

abstract class RouteTrackingViewModel : BaseViewModel() {
    abstract val mapInitialLocation: LiveData<Event<LatLng>>
    abstract val trackingLocationBatch: LiveData<List<TrackingLocationEntity>>
    abstract val trackingStats: LiveData<RouteTrackingStats>
    abstract val trackingStatus: LiveData<@RouteTrackingStatus Int>
    abstract val saveActivitySuccess: LiveData<Event<Unit>>
    abstract val activityType: LiveData<ActivityType>

    abstract fun requestDataUpdates()

    abstract fun cancelDataUpdates()

    abstract fun requestInitialData()
    abstract fun saveActivity(routeMapImage: Bitmap)
    abstract fun resumeDataUpdates()

    abstract fun onSelectActivityType(activityType: ActivityType)
}