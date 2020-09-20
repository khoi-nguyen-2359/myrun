package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.location.model.LatLng
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.model.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.model.TrackingLocationEntity
import akio.apps.myrun.data.workout.model.ActivityType
import akio.apps.myrun.feature._base.utils.flowTimer
import akio.apps.myrun.feature.routetracking.*
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class RouteTrackingViewModelImpl @Inject constructor(
    private val getMapInitialLocationUsecase: GetMapInitialLocationUsecase,
    private val getTrackingLocationUpdatesUsecase: GetTrackingLocationUpdatesUsecase,
    private val routeTrackingState: RouteTrackingState,
    private val saveRouteTrackingWorkoutUsecase: SaveRouteTrackingWorkoutUsecase,
    private val clearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase
) : RouteTrackingViewModel() {

    private val _mapInitialLocation = MutableLiveData<Event<LatLng>>()
    override val mapInitialLocation: LiveData<Event<LatLng>> = _mapInitialLocation

    private val _trackingLocationBatch = MutableLiveData<List<TrackingLocationEntity>>()
    override val trackingLocationBatch: LiveData<List<TrackingLocationEntity>> = _trackingLocationBatch

    private val _trackingStats = MutableLiveData<RouteTrackingStats>()
    override val trackingStats: LiveData<RouteTrackingStats> = _trackingStats

    override val trackingStatus: LiveData<@RouteTrackingStatus Int> = routeTrackingState.getTrackingStatusFlow().asLiveData()

    private val _saveWorkoutSuccess = MutableLiveData<Event<Unit>>()
    override val saveWorkoutSuccess: LiveData<Event<Unit>> = _saveWorkoutSuccess

    private var trackingTimerJob: Job? = null
    private var processedLocationCount = 0

    override fun resumeDataUpdates() {
        if (_mapInitialLocation.value != null && trackingStatus.value == RouteTrackingStatus.RESUMED) {
            requestDataUpdates()
        }
    }

    private fun restoreTrackingStatus(latestStatus: Int) = viewModelScope.launch {
        when (latestStatus) {
            RouteTrackingStatus.RESUMED -> requestDataUpdates()
            RouteTrackingStatus.PAUSED -> notifyDataUpdates()
        }
    }

    override fun initialize() {
        launchCatching {
            val initialLocation = getMapInitialLocationUsecase.getMapInitialLocation()
            _mapInitialLocation.value = Event(initialLocation)

            val latestStatus = routeTrackingState.getTrackingStatus()
            if (latestStatus != RouteTrackingStatus.STOPPED) {
                restoreTrackingStatus(latestStatus)
            }
        }
    }

    override fun saveWorkout(activityType: ActivityType, routeMapImage: Bitmap) {
        launchCatching {
            saveRouteTrackingWorkoutUsecase.saveCurrentWorkout(activityType, routeMapImage)
            clearRouteTrackingStateUsecase.clear()

            _saveWorkoutSuccess.value = Event(Unit)
        }
    }

    private suspend fun notifyDataUpdates() {
        _trackingStats.value = routeTrackingState.run {
            RouteTrackingStats(getRouteDistance(), getInstantSpeed(), getTrackingDuration())
        }

        val batch = getTrackingLocationUpdatesUsecase.getLocationUpdates(processedLocationCount)
        _trackingLocationBatch.value = batch
        processedLocationCount += batch.size
    }

    override fun requestDataUpdates() {
        trackingTimerJob?.cancel()
        trackingTimerJob = viewModelScope.flowTimer(0, TRACKING_TIMER_PERIOD) {
            notifyDataUpdates()
        }
    }

    override fun cancelDataUpdates() {
        trackingTimerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()

        cancelDataUpdates()
    }

    companion object {
        const val TRACKING_TIMER_PERIOD = 1000L
    }
}