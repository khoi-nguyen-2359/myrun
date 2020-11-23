package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.feature.routetracking.model.LatLng
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun._base.utils.flowTimer
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.feature.routetracking.*
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import akio.apps.myrun.feature.strava.ExportActivityToFileUsecase
import akio.apps.myrun.feature.usertimeline.model.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RouteTrackingViewModelImpl @Inject constructor(
    private val appContext: Context,
    private val getMapInitialLocationUsecase: GetMapInitialLocationUsecase,
    private val getTrackedLocationsUsecase: GetTrackedLocationsUsecase,
    private val routeTrackingState: RouteTrackingState,
    private val saveRouteTrackingActivityUsecase: SaveRouteTrackingActivityUsecase,
    private val clearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase,
    private val stravaTokenStorage: StravaTokenStorage,
    private val exportActivityToFileUsecase: ExportActivityToFileUsecase
) : RouteTrackingViewModel() {

    private val _mapInitialLocation = MutableLiveData<Event<LatLng>>()
    override val mapInitialLocation: LiveData<Event<LatLng>> = _mapInitialLocation

    private val _trackingLocationBatch = MutableLiveData<List<TrackingLocationEntity>>()
    override val trackingLocationBatch: LiveData<List<TrackingLocationEntity>> = _trackingLocationBatch

    private val _trackingStats = MutableLiveData<RouteTrackingStats>()
    override val trackingStats: LiveData<RouteTrackingStats> = _trackingStats

    override val trackingStatus: LiveData<@RouteTrackingStatus Int> = routeTrackingState.getTrackingStatusFlow().asLiveData()

    private val _saveActivitySuccess = MutableLiveData<Event<Unit>>()
    override val saveActivitySuccess: LiveData<Event<Unit>> = _saveActivitySuccess

    private val _activityType = MutableLiveData<ActivityType>()
    override val activityType: LiveData<ActivityType> = _activityType

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
            RouteTrackingStatus.PAUSED -> notifyLatestDataUpdate()
        }
    }

    override fun requestInitialData() {
        launchCatching {
            val initialLocation = getMapInitialLocationUsecase.getMapInitialLocation()
            _mapInitialLocation.value = Event(initialLocation)

            _activityType.value = routeTrackingState.getActivityType()

            processedLocationCount = 0
            val latestStatus = routeTrackingState.getTrackingStatus()
            if (latestStatus != RouteTrackingStatus.STOPPED) {
                restoreTrackingStatus(latestStatus)
            }
        }
    }

    override fun saveActivity(routeMapImage: Bitmap) {
        launchCatching {
            val activityType = activityType.value
                ?: return@launchCatching

            val activity = saveRouteTrackingActivityUsecase.saveCurrentActivity(activityType, routeMapImage)
            stravaTokenStorage.getToken()?.let { stravaToken ->
                scheduleStravaActivityUpload(activity)
            }
            routeTrackingState.getStartLocation()?.let { startLocation ->
                scheduleUserRecentPlaceUpdate(startLocation)
            }
            clearRouteTrackingStateUsecase.clear()

            _saveActivitySuccess.value = Event(Unit)
        }
    }

    private suspend fun scheduleStravaActivityUpload(activity: Activity) {
        exportActivityToFileUsecase.exportActivityToFile(activity, false)
    }

    private fun scheduleUserRecentPlaceUpdate(activityStartPoint: LocationEntity) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<UpdateUserRecentPlaceWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
            .setInputData(workDataOf(
                UpdateUserRecentPlaceWorker.INPUT_START_LOCATION_LAT to activityStartPoint.latitude,
                UpdateUserRecentPlaceWorker.INPUT_START_LOCATION_LNG to activityStartPoint.longitude
            ))
            .build()
        WorkManager.getInstance(appContext).enqueue(workRequest)
    }

    private suspend fun notifyLatestDataUpdate() {
        _trackingStats.value = routeTrackingState.run {
            RouteTrackingStats(getRouteDistance(), getInstantSpeed(), getTrackingDuration())
        }

        val batch = getTrackedLocationsUsecase.getTrackedLocations(processedLocationCount)
        _trackingLocationBatch.value = batch
        processedLocationCount += batch.size
    }

    override fun requestDataUpdates() {
        trackingTimerJob?.cancel()
        trackingTimerJob = viewModelScope.flowTimer(0, TRACKING_TIMER_PERIOD) {
            notifyLatestDataUpdate()
        }
    }

    override fun cancelDataUpdates() {
        trackingTimerJob?.cancel()
    }

    override fun onSelectActivityType(activityType: ActivityType) {
        _activityType.value = activityType
        viewModelScope.launch {
            routeTrackingState.setActivityType(activityType)
        }
    }

    companion object {
        const val TRACKING_TIMER_PERIOD = 1000L
    }
}