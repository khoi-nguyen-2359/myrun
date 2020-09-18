package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import akio.apps.myrun.feature._base.flowTimer
import akio.apps.myrun.feature.routetracking.GetMapInitialLocationUsecase
import akio.apps.myrun.feature.routetracking.GetTrackingLocationUpdatesUsecase
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class RouteTrackingViewModelImpl @Inject constructor(
    private val getMapInitialLocationUsecase: GetMapInitialLocationUsecase,
    private val getTrackingLocationUpdatesUsecase: GetTrackingLocationUpdatesUsecase,
    private val routeTrackingState: RouteTrackingState
) : RouteTrackingViewModel() {

    private val _mapInitialLocation = MutableLiveData<Event<Location>>()
    override val mapInitialLocation: LiveData<Event<Location>> = _mapInitialLocation

    private val _trackingLocationBatch = MutableLiveData<List<TrackingLocationEntity>>()
    override val trackingLocationBatch: LiveData<List<TrackingLocationEntity>> = _trackingLocationBatch

    private val _trackingStats = MutableLiveData<RouteTrackingStats>()
    override val trackingStats: LiveData<RouteTrackingStats> = _trackingStats

    private var trackingTimerJob: Job? = null
    private var trackingDuration: Long = 0

    init {
        viewModelScope.launch {
            trackingDuration = routeTrackingState.getTrackingDuration()
        }
    }

    override fun requestMapInitialLocation() {
        launchCatching {
            val initialLocation = getMapInitialLocationUsecase.getMapInitialLocation()
            _mapInitialLocation.value = Event(initialLocation)
        }
    }

    override fun requestRouteTrackingLocationUpdate(skip: Int) {
        viewModelScope.launch {
            val batch = getTrackingLocationUpdatesUsecase.getLocationUpdates(skip)
            _trackingLocationBatch.value = batch
        }
    }

    override fun startTrackingStatsUpdates() {
        viewModelScope.launch {
            trackingTimerJob?.cancel()
            trackingTimerJob = flowTimer(0, TRACKING_TIMER_PERIOD, onTrackingTimerTick)
        }
    }

    private val onTrackingTimerTick: () -> Unit = {
        viewModelScope.launch {
            trackingDuration += TRACKING_TIMER_PERIOD
            _trackingStats.value = RouteTrackingStats(routeTrackingState.getRouteDistance(), routeTrackingState.getInstantSpeed(), trackingDuration / 1000)
        }
    }

    override fun resumeTrackingStatsUpdates() {
        viewModelScope.launch {
            if (routeTrackingState.isTrackingInProgress()) {
                startTrackingStatsUpdates()
            }
        }
    }

    override fun stopTrackingStatsUpdates() {
        trackingTimerJob?.cancel()
    }

    companion object {
        const val TRACKING_TIMER_PERIOD = 1000L
    }
}