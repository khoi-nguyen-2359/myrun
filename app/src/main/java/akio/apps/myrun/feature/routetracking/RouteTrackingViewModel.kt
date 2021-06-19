package akio.apps.myrun.feature.routetracking

import akio.apps._base.lifecycle.Event
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.location.LocationRequestEntity
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingStats
import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class RouteTrackingViewModel : BaseViewModel() {
    abstract val isStopOptionDialogShowing: MutableStateFlow<Boolean>
    abstract val trackingLocationBatch: LiveData<List<TrackingLocationEntity>>
    abstract val trackingStats: LiveData<RouteTrackingStats>
    abstract val trackingStatus: LiveData<@RouteTrackingStatus Int>
    abstract val isStoreActivityDone: LiveData<Event<Unit>>
    abstract val activityType: LiveData<ActivityType>
    abstract suspend fun getInitialLocation(): Location?

    abstract fun getLocationUpdate(locationRequest: LocationRequestEntity): Flow<List<Location>>

    abstract fun requestDataUpdates()
    abstract fun cancelDataUpdates()
    abstract fun requestInitialData()
    abstract suspend fun storeActivityData(routeMapImage: Bitmap)
    abstract fun resumeDataUpdates()

    abstract fun onSelectActivityType(activityType: ActivityType)
}
