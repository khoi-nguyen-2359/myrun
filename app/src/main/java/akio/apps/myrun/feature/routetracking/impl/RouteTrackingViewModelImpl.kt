package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.lifecycle.Event
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.flowTimer
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.data.routetracking.model.LatLng
import akio.apps.myrun.domain.activityexport.ClearExportActivityLocationUsecase
import akio.apps.myrun.domain.activityexport.SaveExportActivityLocationsUsecase
import akio.apps.myrun.domain.routetracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.routetracking.GetMapInitialLocationUsecase
import akio.apps.myrun.domain.routetracking.GetTrackedLocationsUsecase
import akio.apps.myrun.domain.routetracking.SaveRouteTrackingActivityUsecase
import akio.apps.myrun.domain.strava.ExportTrackingActivityToStravaFileUsecase
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.strava.impl.UploadStravaFileWorker
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RouteTrackingViewModelImpl @Inject constructor(
    private val application: Application,
    private val getMapInitialLocationUsecase: GetMapInitialLocationUsecase,
    private val getTrackedLocationsUsecase: GetTrackedLocationsUsecase,
    private val routeTrackingState: RouteTrackingState,
    private val saveRouteTrackingActivityUsecase: SaveRouteTrackingActivityUsecase,
    private val clearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase,
    private val saveExportActivityLocationsUsecase: SaveExportActivityLocationsUsecase,
    private val exportActivityToStravaFileUsecase: ExportTrackingActivityToStravaFileUsecase,
    private val clearExportActivityLocationUsecase: ClearExportActivityLocationUsecase,
    private val activityMapper: ActivityModelMapper,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val authenticationState: UserAuthenticationState
) : RouteTrackingViewModel() {

    private val _mapInitialLocation = MutableLiveData<Event<LatLng>>()
    override val mapInitialLocation: LiveData<Event<LatLng>> = _mapInitialLocation

    private val _trackingLocationBatch = MutableLiveData<List<TrackingLocationEntity>>()
    override val trackingLocationBatch: LiveData<List<TrackingLocationEntity>> =
        _trackingLocationBatch

    private val _trackingStats = MutableLiveData<RouteTrackingStats>()
    override val trackingStats: LiveData<RouteTrackingStats> = _trackingStats

    override val trackingStatus: LiveData<@RouteTrackingStatus Int> =
        routeTrackingState.getTrackingStatusFlow()
            .asLiveData()

    private val _saveActivitySuccess = MutableLiveData<Event<Unit>>()
    override val saveActivitySuccess: LiveData<Event<Unit>> = _saveActivitySuccess

    private val _activityType = MutableLiveData<ActivityType>()
    override val activityType: LiveData<ActivityType> = _activityType

    private var trackingTimerJob: Job? = null
    private var processedLocationCount = 0

    private val activityTypeNameMap: Map<ActivityType, Int> = mapOf(
        ActivityType.Running to R.string.activity_name_running,
        ActivityType.Cycling to R.string.activity_name_cycling
    )

    override fun resumeDataUpdates() {
        if (_mapInitialLocation.value != null &&
            trackingStatus.value == RouteTrackingStatus.RESUMED
        ) {
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

            val activityName = makeActivityName(activityType)
            val activity =
                saveRouteTrackingActivityUsecase.saveCurrentActivity(
                    activityType,
                    activityName,
                    routeMapImage
                )
            mayScheduleStravaActivityUpload(activityMapper.map(activity))

            clearRouteTrackingStateUsecase.clear()

            _saveActivitySuccess.value = Event(Unit)
        }
    }

    private fun makeActivityName(activityType: ActivityType): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> application.getString(
                R.string.item_activity_title_morning,
                application.getString(activityTypeNameMap[activityType] ?: 0)
            )
            in 12..16 -> application.getString(
                R.string.item_activity_title_afternoon,
                application.getString(activityTypeNameMap[activityType] ?: 0)
            )
            else -> application.getString(
                R.string.item_activity_title_evening,
                application.getString(activityTypeNameMap[activityType] ?: 0)
            )
        }
    }

    private suspend fun mayScheduleStravaActivityUpload(activity: Activity) {
        val userAccountId = authenticationState.getUserAccountId()
        if (userAccountId == null ||
            externalAppProvidersRepository.getStravaProviderToken(userAccountId) == null
        ) {
            return
        }

        saveExportActivityLocationsUsecase(activity.id)
        GlobalScope.launch {
            exportActivityToStravaFileUsecase(activityMapper.mapRev(activity), false)
            clearExportActivityLocationUsecase(activity.id)
            UploadStravaFileWorker.enqueueForFinishedActivity(application)
        }
    }

    private suspend fun notifyLatestDataUpdate() {
        _trackingStats.value = routeTrackingState.run {
            RouteTrackingStats(getRouteDistance(), getInstantSpeed(), getTrackingDuration())
        }

        val batch = getTrackedLocationsUsecase.getTrackedLocations(processedLocationCount)
        if (batch.isNotEmpty()) {
            _trackingLocationBatch.value = batch
            processedLocationCount += batch.size
        }
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
