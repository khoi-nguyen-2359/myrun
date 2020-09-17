package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import akio.apps.myrun.feature.routetracking.GetMapInitialLocationUsecase
import akio.apps.myrun.feature.routetracking.GetTrackingLocationUpdatesUsecase
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import android.location.Location
import androidx.lifecycle.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RouteTrackingViewModelImpl @Inject constructor(
    private val getMapInitialLocationUsecase: GetMapInitialLocationUsecase,
    private val getTrackingLocationUpdatesUsecase: GetTrackingLocationUpdatesUsecase
) : RouteTrackingViewModel() {

    private val _mapInitialLocation = MutableLiveData<Event<Location>>()
    override val mapInitialLocation: LiveData<Event<Location>> = _mapInitialLocation

    private val _trackingLocationBatch = MutableLiveData<List<TrackingLocationEntity>>()
    override val trackingLocationBatch: LiveData<List<TrackingLocationEntity>> = _trackingLocationBatch

    override fun requestMapInitialLocation() {
        launchCatching {
            val initialLocation = getMapInitialLocationUsecase.getMapInitialLocation()
            _mapInitialLocation.value = Event(initialLocation)
        }
    }

    override fun requestRouteTrackingLocationUpdate(skip: Int) {
        Timber.d("viewmodel: requestRouteTrackingLocationUpdate $skip")
        viewModelScope.launch {
            val batch = getTrackingLocationUpdatesUsecase.getLocationUpdates(skip)
                .first()
            _trackingLocationBatch.value = batch
        }
    }
}