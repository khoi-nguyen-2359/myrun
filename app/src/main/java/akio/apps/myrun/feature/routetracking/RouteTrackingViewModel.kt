package akio.apps.myrun.feature.routetracking

import akio.apps.common.lifecycle.Event
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

abstract class RouteTrackingViewModel: ViewModel() {
    abstract val mapInitLocation: LiveData<Event<Location>>
}