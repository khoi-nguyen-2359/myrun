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
import androidx.lifecycle.*
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

    private var trackingLocationBatchSource: LiveData<List<TrackingLocationEntity>>? = null
    private val _trackingLocationBatch = MediatorLiveData<List<TrackingLocationEntity>>()
    override val trackingLocationBatch: LiveData<List<TrackingLocationEntity>> = _trackingLocationBatch

    private val _trackingStats = MutableLiveData<RouteTrackingStats>()
    override val trackingStats: LiveData<RouteTrackingStats> = _trackingStats

    private val _trackingStatus = MutableLiveData(RouteTrackingStatus.Stopped)
    override val trackingStatus: LiveData<RouteTrackingStatus> = _trackingStatus

    private var trackingTimerJob: Job? = null
    private var processedLocationCount = 0

    override fun restoreTrackingStatus() {
        viewModelScope.launch {
            if (routeTrackingState.isTrackingInProgress()) {
                startRouteTracking()
            }
        }
    }

    override fun requestMapInitialLocation() {
        launchCatching {
            val initialLocation = getMapInitialLocationUsecase.getMapInitialLocation()
            _mapInitialLocation.value = Event(initialLocation)
        }
    }

    override fun startRouteTracking() {
        _trackingStatus.value = RouteTrackingStatus.Resumed
        requestDataUpdates()
    }

    override fun pauseRouteTracking() {
        _trackingStatus.value = RouteTrackingStatus.Paused
        cancelDataUpdates()
    }

    override fun resumeRouteTracking() {
        _trackingStatus.value = RouteTrackingStatus.Resumed
        requestDataUpdates()
    }

    override fun stopRouteTracking() {
        _trackingStatus.value = RouteTrackingStatus.Stopped
        cancelDataUpdates()
    }

    private val onTrackingTimerTick: () -> Unit = {
        viewModelScope.launch {
            val trackingDuration = System.currentTimeMillis() - routeTrackingState.getLastResumeTime() + routeTrackingState.getTrackingDuration()
            _trackingStats.value = RouteTrackingStats(routeTrackingState.getRouteDistance(), routeTrackingState.getInstantSpeed(), trackingDuration / 1000)
        }
    }

    override fun requestDataUpdates() {
        viewModelScope.launch {
            if (_trackingStatus.value == RouteTrackingStatus.Resumed) {
                trackingTimerJob?.cancel()
                trackingTimerJob = flowTimer(TRACKING_TIMER_PERIOD, TRACKING_TIMER_PERIOD, onTrackingTimerTick)

                trackingLocationBatchSource?.let {
                    _trackingLocationBatch.removeSource(it)
                }
                trackingLocationBatchSource = getTrackingLocationUpdatesUsecase.getLocationUpdates(processedLocationCount)
                    .asLiveData()
                    .also {
                        _trackingLocationBatch.addSource(it) { batch ->
                            _trackingLocationBatch.value = batch
                            processedLocationCount += batch.size
                        }
                    }
            }
        }
    }

    override fun cancelDataUpdates() {
        trackingTimerJob?.cancel()
        trackingLocationBatchSource?.let {
            _trackingLocationBatch.removeSource(it)
        }
    }

    override fun onCleared() {
        super.onCleared()

        cancelDataUpdates()
    }

    companion object {
        const val TRACKING_TIMER_PERIOD = 1000L
    }
}