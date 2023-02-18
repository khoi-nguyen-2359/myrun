package akio.apps.myrun.feature.feed

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserPreferences
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
import akio.apps.myrun.feature.core.BaseViewModel
import akio.apps.myrun.feature.feed.model.FeedActivity
import akio.apps.myrun.feature.feed.model.FeedSuggestedUserFollow
import akio.apps.myrun.feature.feed.model.FeedUiModel
import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestionList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedViewModel @Inject constructor(
    private val activityLocalStorage: ActivityLocalStorage,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val userAuthenticationState: UserAuthenticationState,
    private val userPreferences: UserPreferences,
    private val activityPagingSourceFactory: ActivityPagingSourceFactory,
    private val userRecentActivityRepository: UserRecentActivityRepository,
    private val placeNameSelector: PlaceNameSelector,
    private val getUserFollowUsecase: GetUserFollowSuggestionUsecase,
    private val followUserUsecase: FollowUserUsecase,
    private val userFollowRepository: UserFollowRepository,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
    private val pagingDataScope: CoroutineScope,
) : BaseViewModel() {

    private val userId: String = userAuthenticationState.requireUserAccountId()

    private val recentPlaceIdentifierFlow: Flow<PlaceIdentifier?> = flow {
        emit(userRecentActivityRepository.getRecentPlaceIdentifier(userId))
    }.flowOn(Dispatchers.IO)

    private val followRequestedStateMap: SnapshotStateMap<String, Boolean> = mutableStateMapOf()
    private val followRequestedMapFlow: Flow<Map<String, Boolean>> = snapshotFlow {
        // It is recommended to use toMap() here:
        // https://developer.android.com/reference/kotlin/androidx/compose/runtime/snapshots/SnapshotStateMap#toMap()
        followRequestedStateMap.toMap()
    }

    private val followSuggestionFlow: Flow<List<UserFollowSuggestion>> = flow {
        emit(getUserFollowUsecase.getFollowSuggestion())
    }.flowOn(Dispatchers.IO)

    init {
        Timber.d("create FeedViewModel")
        observeUserFollowings()
    }

    private fun observeUserFollowings() = pagingDataScope.launch {
        userFollowRepository.getUserFollowingsFlow(userId)
            .flowOn(Dispatchers.IO)
            .collect { followings ->
                val requestedUidList = followings.map { following -> following.uid }
                    .associateWith { true }
                followRequestedStateMap.putAll(requestedUidList)
            }
    }

    val activityPagingFlow = Pager(
        config = PagingConfig(
            pageSize = ActivityFeedViewModel.PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = ActivityFeedViewModel.PAGE_SIZE
        )
        // do not pass initial key as timestamp because initialKey is reused in invalidating!
        // initialKey = System.currentTimeMillis()
    ) { recreateActivityPagingSource() }
        .flow
        .cachedIn(pagingDataScope)
        .let { pagingFlow ->
            combine(
                pagingFlow,
                followSuggestionFlow,
                followRequestedMapFlow,
                recentPlaceIdentifierFlow,
                ::combinePagingDataSources
            )
        }
        .cachedIn(pagingDataScope)

    private var activityPagingSource: ActivityPagingSource? = null

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
        Timber.d("combine paging data")
        var index = 0
        return pagingData.flatMap { baseActivity ->
            val locationName = getActivityDisplayLocation(baseActivity, recentPlaceIdentifier)
            val formattedStartTime = activityDateTimeFormatter.formatActivityDateTime(baseActivity.startTime)
            if (index++ == ActivityFeedViewModel.PAGE_SIZE / 2 && userFollows.isNotEmpty()) {
                // insert follow suggestion in the end of the first page
                val feedModels = userFollows.map { dataModel ->
                    FeedSuggestedUserFollow(
                        dataModel,
                        userFollowsRequestedMap[dataModel.uid] ?: false
                    )
                }
                listOf(
                    FeedActivity(
                        baseActivity,
                        locationName,
                        formattedStartTime,
                        baseActivity.athleteInfo.userId == userId
                    ),
                    FeedUserFollowSuggestionList(feedModels)
                )
            } else {
                listOf(
                    FeedActivity(
                        baseActivity,
                        locationName,
                        formattedStartTime,
                        baseActivity.athleteInfo.userId == userId
                    )
                )
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

    @Composable
    fun getUploadingActivityCount(): Int = rememberFlow(
        flow = remember {
            activityLocalStorage.getActivityStorageDataCountFlow().distinctUntilChanged()
        }, default = 0
    )

    @Composable
    fun getUserProfile(): UserProfile? = rememberFlow(
        flow = remember {
            getUserProfileUsecase.getUserProfileFlow(userId).mapNotNull { it.data }
        }, default = null
    )

    @Composable
    fun getMeasureSystem(): MeasureSystem = rememberFlow(
        flow = remember { userPreferences.getMeasureSystem() },
        default = MeasureSystem.Default
    )
}
