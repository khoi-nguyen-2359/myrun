package akio.apps.myrun.feature.home.feed

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.time.Now
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.domain.launchcatching.Event
import akio.apps.myrun.domain.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.PlaceNameSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber

class ActivityFeedViewModel @Inject constructor(
    private val activityPagingSourceFactory: ActivityPagingSourceFactory,
    private val placeNameSelector: PlaceNameSelector,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
    private val launchCatchingViewModel: LaunchCatchingDelegate,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingViewModel {

    private var activityPagingSource: ActivityPagingSource? = null

    private var lastActivityStorageCount: Int = -1

    val activityUploadBadge: Flow<ActivityUploadBadgeStatus> =
        createActivityUploadBadgeStatusFlow()

    val userProfile: Flow<UserProfile> =
        getUserProfileUsecase.getUserProfileFlow().mapNotNull { it.data }

    @OptIn(FlowPreview::class)
    private fun createActivityUploadBadgeStatusFlow(): Flow<ActivityUploadBadgeStatus> =
        activityLocalStorage.getActivityStorageDataCountFlow()
            .distinctUntilChanged()
            .mapNotNull { count ->
                val status = when {
                    count > 0 -> ActivityUploadBadgeStatus.InProgress(count)
                    count == 0 && lastActivityStorageCount > 0 -> ActivityUploadBadgeStatus.Complete
                    else -> null
                }
                lastActivityStorageCount = count

                status
            }
            // there are 2 collectors so use shareIn
            .shareIn(viewModelScope, replay = 1, started = SharingStarted.WhileSubscribed())

    val myActivityList: Flow<PagingData<ActivityModel>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE
        ),
        // do not pass initial key as timestamp because initialKey is reused in invalidating!
        // initialKey = Now.currentTimeMillis()
    ) { recreateActivityPagingSource() }
        .flow
        .cachedIn(viewModelScope)

    private fun recreateActivityPagingSource(): ActivityPagingSource =
        activityPagingSourceFactory().also {
            Timber.d("recreateActivityPagingSource")
            activityPagingSource = it
        }

    private val mapActivityIdToPlaceName: MutableMap<String, String> = mutableMapOf()

    // This is optional data, null for not found.
    private var userRecentPlaceIdentifier: PlaceIdentifier? = null

    private val isLoadingInitialDataMutable: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoadingInitialData: Flow<Boolean> = isLoadingInitialDataMutable

    init {
        loadInitialData()
        observeActivityUploadCount()
    }

    private fun observeActivityUploadCount() = viewModelScope.launch {
        activityUploadBadge.collect {
            reloadFeedData()
        }
    }

    private fun reloadFeedData() {
        Timber.d("reloadFeedData")
        activityPagingSource?.invalidate()
    }

    fun getFormattedStartTime(activity: ActivityModel): ActivityDateTimeFormatter.Result =
        activityDateTimeFormatter.formatActivityDateTime(activity.startTime)

    fun getActivityDisplayPlaceName(activity: ActivityModel): String {
        val activityPlaceIdentifier = activity.placeIdentifier
            ?: return ""
        val activityId = activity.id
        var placeName = mapActivityIdToPlaceName[activityId]
        if (placeName != null) {
            return placeName
        }

        placeName = placeNameSelector(
            activityPlaceIdentifier,
            userRecentPlaceIdentifier
        )
            ?: ""
        mapActivityIdToPlaceName[activityId] = placeName

        return placeName
    }

    private fun loadInitialData() =
        viewModelScope.launchCatching(
            progressStateFlow = isLoadingInitialDataMutable,
            errorStateFlow = MutableStateFlow(Event(null))
        ) {
            val userId = userAuthenticationState.getUserAccountId()
            if (userId != null) {
                userRecentPlaceIdentifier =
                    userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
            }
        }

    sealed class ActivityUploadBadgeStatus {
        class InProgress(val activityCount: Int) : ActivityUploadBadgeStatus()
        object Complete : ActivityUploadBadgeStatus()
        object Hidden : ActivityUploadBadgeStatus()
    }

    companion object {
        const val PAGE_SIZE = 6
    }
}
