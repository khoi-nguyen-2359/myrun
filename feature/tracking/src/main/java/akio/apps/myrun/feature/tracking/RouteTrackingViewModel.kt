package akio.apps.myrun.feature.tracking

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.location.api.LOG_TAG_LOCATION
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.location.api.model.LocationRequestConfig
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.domain.tracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.tracking.StoreTrackingActivityDataUsecase
import akio.apps.myrun.feature.core.flowTimer
import akio.apps.myrun.feature.tracking.model.RouteTrackingStats
import akio.apps.myrun.worker.UploadStravaFileWorker
import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import timber.log.Timber

internal class RouteTrackingViewModel @Inject constructor(
    private val application: Application,
    private val routeTrackingState: RouteTrackingState,
    private val clearRouteTrackingStateUsecase: ClearRouteTrackingStateUsecase,
    private val storeTrackingActivityDataUsecase: StoreTrackingActivityDataUsecase,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val authenticationState: UserAuthenticationState,
    private val locationDataSource: LocationDataSource,
    private val routeTrackingConfiguration: RouteTrackingConfiguration,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    // TODO: Will refactor this screen to Composable
    val isStopOptionDialogShowing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val stickyCameraButtonState: MutableStateFlow<RouteTrackingActivity.CameraMovement> =
        MutableStateFlow(RouteTrackingActivity.CameraMovement.StickyBounds)

    suspend fun getLatestLocation(): Location =
        locationUpdateFlow.takeWhile { it.isNotEmpty() }.first().first()

    private val _trackingLocationBatch = MutableLiveData<List<ActivityLocation>>()
    val trackingLocationBatch: LiveData<List<ActivityLocation>> =
        _trackingLocationBatch

    private val _trackingStats = MutableLiveData<RouteTrackingStats>()
    val trackingStats: LiveData<RouteTrackingStats> = _trackingStats

    val trackingStatus: LiveData<@RouteTrackingStatus Int> =
        routeTrackingState.getTrackingStatusFlow().asLiveData()

    private val _activityType = MutableLiveData<ActivityType>()
    val activityType: LiveData<ActivityType> = _activityType

    private var trackingTimerJob: Job? = null
    private var processedLocationCount = 0

    private val activityTypeNameMap: Map<ActivityType, Int> = mapOf(
        ActivityType.Running to R.string.activity_name_running,
        ActivityType.Cycling to R.string.activity_name_cycling
    )

    @OptIn(FlowPreview::class)
    val locationUpdateFlow: Flow<List<Location>> =
        getLocationRequestConfigFlow().flatMapConcat(locationDataSource::getLocationUpdate)

    fun resumeDataUpdates() {
        if (trackingStatus.value == RouteTrackingStatus.RESUMED) {
            requestDataUpdates()
        }
    }

    private fun restoreTrackingStatus(latestStatus: Int) = viewModelScope.launch {
        when (latestStatus) {
            RouteTrackingStatus.RESUMED -> requestDataUpdates()
            RouteTrackingStatus.PAUSED -> notifyLatestDataUpdate()
        }
    }

    fun requestInitialData() {
        viewModelScope.launchCatching {
            _activityType.value = routeTrackingState.getActivityType()

            processedLocationCount = 0
            val latestStatus = routeTrackingState.getTrackingStatus()
            if (latestStatus != RouteTrackingStatus.STOPPED) {
                restoreTrackingStatus(latestStatus)
            }
        }
    }

    suspend fun storeActivityData(routeMapImage: Bitmap) {
        val activityType = activityType.value
            ?: return
        val activityName = makeNewActivityName(activityType)
        storeTrackingActivityDataUsecase(activityName, routeMapImage)
        scheduleActivitySyncIfAvailable()
        clearRouteTrackingStateUsecase.clear()
    }

    fun getLocationRequestConfigFlow(): Flow<LocationRequestConfig> =
        routeTrackingConfiguration.getLocationRequestConfigFlow()

    private fun makeNewActivityName(activityType: ActivityType): String {
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

    private suspend fun scheduleActivitySyncIfAvailable() {
        val userAccountId = authenticationState.getUserAccountId()
        if (userAccountId != null && externalAppProvidersRepository.isStravaSyncEnabled()) {
            UploadStravaFileWorker.enqueue(application)
        }
    }

    private suspend fun notifyLatestDataUpdate() {
        _trackingStats.value = routeTrackingState.run {
            RouteTrackingStats(getRouteDistance(), getInstantSpeed(), getTrackingDuration())
        }

        val batch = routeTrackingLocationRepository.getTrackedLocations(processedLocationCount)
        if (batch.isNotEmpty()) {
            Timber.tag(LOG_TAG_LOCATION)
                .d("[RouteTrackingViewModel] notifyLatestDataUpdate: ${batch.size}")
            _trackingLocationBatch.value = batch
            processedLocationCount += batch.size
        }
    }

    fun requestDataUpdates() {
        trackingTimerJob?.cancel()
        trackingTimerJob = viewModelScope.flowTimer(0, TRACKING_TIMER_PERIOD) {
            notifyLatestDataUpdate()
        }
    }

    fun cancelDataUpdates() {
        trackingTimerJob?.cancel()
    }

    fun onSelectActivityType(activityType: ActivityType) {
        _activityType.value = activityType
        viewModelScope.launch {
            routeTrackingState.setActivityType(activityType)
        }
    }

    fun discardActivity() {
        viewModelScope.launchCatching {
            clearRouteTrackingStateUsecase.clear()
        }
    }

    companion object {
        const val TRACKING_TIMER_PERIOD = 1000L
    }
}
