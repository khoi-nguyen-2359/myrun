package akio.apps.myrun.feature.feed

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.CurrentUserPreferences
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.domain.user.FollowUserUsecase
import akio.apps.myrun.domain.user.GetUserFollowSuggestionUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.PlaceNameSelector
import akio.apps.myrun.feature.feed.model.FeedActivity
import akio.apps.myrun.feature.feed.model.FeedSuggestedUserFollow
import akio.apps.myrun.feature.feed.model.FeedUiModel
import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestionList
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class FeedViewModel @Inject constructor(
    private val activityLocalStorage: ActivityLocalStorage,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val userAuthenticationState: UserAuthenticationState,
    private val currentUserPreferences: CurrentUserPreferences,
    private val activityPagingSourceFactory: ActivityPagingSourceFactory,
    private val userRecentActivityRepository: UserRecentActivityRepository,
    private val placeNameSelector: PlaceNameSelector,
    private val getUserFollowUsecase: GetUserFollowSuggestionUsecase,
    private val followUserUsecase: FollowUserUsecase,
    private val userFollowRepository: UserFollowRepository,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
    private val viewModelScope: CoroutineScope,
) {

    private val userId: String = userAuthenticationState.requireUserAccountId()

    private val recentPlaceIdentifierFlow: Flow<PlaceIdentifier?> = flow {
        emit(null)
        emit(userRecentActivityRepository.getRecentPlaceIdentifier(userId, useCache = false))
    }
        .flowOn(Dispatchers.IO)

    private val followRequestedStateMap: SnapshotStateMap<String, Boolean> = mutableStateMapOf()
    private val followRequestedMapFlow: Flow<Map<String, Boolean>> = snapshotFlow {
        // It is recommended to use toMap() here:
        // https://developer.android.com/reference/kotlin/androidx/compose/runtime/snapshots/SnapshotStateMap#toMap()
        followRequestedStateMap.toMap()
    }

    private val followSuggestionFlow: Flow<List<UserFollowSuggestion>> = flow {
        emit(emptyList())
        emit(getUserFollowUsecase.getFollowSuggestion())
    }
        .flowOn(Dispatchers.IO)

    val userProfileFlow: Flow<UserProfile?> =
        getUserProfileUsecase.getUserProfileFlow(userId).mapNotNull { it.data }

    val activityPagingFlow = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE
        )
        // do not pass initial key as timestamp because initialKey is reused in invalidating!
        // initialKey = System.currentTimeMillis()
    ) { recreateActivityPagingSource() }
        .flow
        .cachedIn(viewModelScope)
        .let { pagingFlow ->
            combine(
                pagingFlow,
                followSuggestionFlow,
                followRequestedMapFlow,
                recentPlaceIdentifierFlow,
                ::combinePagingDataSources
            )
        }
        .cachedIn(viewModelScope)

    private var activityPagingSource: ActivityPagingSource? = null

    val uploadingActivityCountFlow: Flow<Int> =
        activityLocalStorage.getActivityStorageDataCountFlow().distinctUntilChanged()

    val measureSystem: Flow<MeasureSystem> = currentUserPreferences.getMeasureSystemFlow()

    init {
        Timber.d("create FeedViewModel")
        observeUserFollowings()
        observeActivityUploadCount()
    }

    private fun observeUserFollowings() = viewModelScope.launch {
        userFollowRepository.getUserFollowingsFlow(userId)
            .flowOn(Dispatchers.IO)
            .collect { followings ->
                val requestedUidList = followings.map { following -> following.uid }
                    .associateWith { true }
                followRequestedStateMap.putAll(requestedUidList)
            }
    }

    private fun recreateActivityPagingSource(): ActivityPagingSource =
        activityPagingSourceFactory.createPagingSource().also {
            Timber.d("recreateActivityPagingSource")
            activityPagingSource = it
        }

    private fun combinePagingDataSources(
        pagingData: PagingData<BaseActivityModel>,
        userFollows: List<UserFollowSuggestion>,
        userFollowsRequestedMap: Map<String, Boolean>,
        recentPlaceIdentifier: PlaceIdentifier?,
    ): PagingData<FeedUiModel> {
        Timber.d("combine paging data sources")
        var index = 0
        return pagingData.flatMap { baseActivity ->
            val locationName = getActivityDisplayLocation(baseActivity, recentPlaceIdentifier)
            val formattedStartTime =
                activityDateTimeFormatter.formatActivityDateTime(baseActivity.startTime)
            val activityItem = FeedActivity(
                baseActivity,
                locationName,
                formattedStartTime,
                baseActivity.athleteInfo.userId == userId
            )
            if (index++ == PAGE_SIZE / 2 && userFollows.isNotEmpty()) {
                // insert follow suggestion item in the middle of the first page
                val suggestionFeedModels = userFollows.map { suggestionData ->
                    FeedSuggestedUserFollow(
                        suggestionData,
                        userFollowsRequestedMap[suggestionData.uid] ?: false
                    )
                }
                listOf(activityItem, FeedUserFollowSuggestionList(suggestionFeedModels))
            } else {
                listOf(activityItem)
            }
        }
    }

    private fun getActivityDisplayLocation(
        activity: BaseActivityModel,
        userPlaceIdentifier: PlaceIdentifier?,
    ): String {
        val activityPlaceIdentifier = activity.placeIdentifier
            ?: return ""
        return placeNameSelector.select(activityPlaceIdentifier, userPlaceIdentifier)
            ?: ""
    }

    private fun observeActivityUploadCount() = viewModelScope.launch {
        activityLocalStorage.getActivityStorageDataCountFlow()
            .distinctUntilChanged()
            .collect { reloadFeedData() }
    }

    private fun reloadFeedData() {
        Timber.d("reloadFeedData")
        activityPagingSource?.invalidate()
    }

    suspend fun followUser(userFollowSuggestion: UserFollowSuggestion) {
        try {
            followRequestedStateMap[userFollowSuggestion.uid] = true
            withContext(Dispatchers.IO) {
                followUserUsecase.followUser(userFollowSuggestion)
                Timber.d("follow user ${userFollowSuggestion.displayName}")
            }
        } catch (ex: Exception) {
            followRequestedStateMap[userFollowSuggestion.uid] = false
            throw ex
        }
    }

    companion object {
        const val PAGE_SIZE = 6
    }
}
