package akio.apps.myrun.feature.routetracking

import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
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
    abstract val activityType: LiveData<ActivityType>
    abstract suspend fun getInitialLocation(): Location?

    abstract suspend fun getLocationUpdate(): Flow<List<Location>>

    abstract fun requestDataUpdates()
    abstract fun cancelDataUpdates()
    abstract fun requestInitialData()
    abstract suspend fun storeActivityData(routeMapImage: Bitmap)
    abstract fun resumeDataUpdates()

    abstract fun onSelectActivityType(activityType: ActivityType)
    abstract fun discardActivity()
    abstract suspend fun getLocationRequestConfig(): LocationRequestConfig
}
