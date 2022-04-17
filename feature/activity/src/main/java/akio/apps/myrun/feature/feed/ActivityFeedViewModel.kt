package akio.apps.myrun.feature.feed

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.feature.core.Event
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.PlaceNameSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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

internal class ActivityFeedViewModel @Inject constructor(
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

    val myActivityList: Flow<PagingData<BaseActivityModel>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE
        ),
        // do not pass initial key as timestamp because initialKey is reused in invalidating!
        // initialKey = System.currentTimeMillis()
    ) { recreateActivityPagingSource() }
        .flow

    private val mapActivityIdToPlaceName: MutableMap<String, String> = mutableMapOf()

    // This is optional data, null for not found.
    private var userRecentPlaceIdentifier: PlaceIdentifier? = null

    private val isInitialLoadingMutable: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isInitialLoading: Flow<Boolean> = isInitialLoadingMutable

    init {
        loadInitialData()
        observeActivityUploadCount()
    }

    fun getFormattedStartTime(activity: BaseActivityModel): ActivityDateTimeFormatter.Result =
        activityDateTimeFormatter.formatActivityDateTime(activity.startTime)

    fun getActivityDisplayPlaceName(activity: BaseActivityModel): String {
        val activityPlaceIdentifier = activity.placeIdentifier
            ?: return ""
        val activityId = activity.id
        var placeName = mapActivityIdToPlaceName[activityId]
        if (placeName != null) {
            return placeName
        }

        placeName = placeNameSelector.select(activityPlaceIdentifier, userRecentPlaceIdentifier)
            ?: ""
        mapActivityIdToPlaceName[activityId] = placeName

        return placeName
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
            // convert to shared flow for multiple collectors
            .shareIn(viewModelScope, replay = 1, started = SharingStarted.WhileSubscribed())

    private fun recreateActivityPagingSource(): ActivityPagingSource =
        activityPagingSourceFactory.createPagingSource().also {
            Timber.d("recreateActivityPagingSource")
            activityPagingSource = it
        }

    private fun loadInitialData() =
        viewModelScope.launchCatching(
            progressStateFlow = isInitialLoadingMutable,
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
