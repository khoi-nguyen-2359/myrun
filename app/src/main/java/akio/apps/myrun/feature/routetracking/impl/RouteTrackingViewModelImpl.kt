package akio.apps.myrun.feature.routetracking.impl

import akio.apps.common.lifecycle.Event
import akio.apps.myrun.data.location.LocationDataSource
import akio.apps.myrun.feature.routetracking.GetMapInitialLocationUsecase
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class RouteTrackingViewModelImpl @Inject constructor(
    private val getMapInitialLocationUsecase: GetMapInitialLocationUsecase
): RouteTrackingViewModel() {

    private val _mapInitLocation = MutableLiveData<Event<Location>>()
    override val mapInitLocation: LiveData<Event<Location>> = _mapInitLocation

    init {
        getMapInitLocation()
    }

    private fun getMapInitLocation() {
        viewModelScope.launch {
            val lastLocation = getMapInitialLocationUsecase.getMapInitialLocation()
            _mapInitLocation.value = Event(lastLocation)
        }
    }
}