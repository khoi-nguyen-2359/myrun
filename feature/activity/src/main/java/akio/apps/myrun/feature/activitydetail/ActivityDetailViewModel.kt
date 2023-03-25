package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.CurrentUserPreferences
import akio.apps.myrun.data.user.api.UserPreferencesRepository
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.domain.activity.ActivitySplitCalculator
import akio.apps.myrun.domain.user.PlaceNameSelector
import androidx.lifecycle.SavedStateHandle
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class ActivityDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val userRecentActivityRepository: UserRecentActivityRepository,
    private val userAuthState: UserAuthenticationState,
    private val placeNameSelector: PlaceNameSelector,
    private val activitySplitCalculator: ActivitySplitCalculator,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
    currentUserPreferences: CurrentUserPreferences,
    private val userPrefRepository: UserPreferencesRepository,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {

    private val activityRefreshSignalFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val activityDetailsFlow: Flow<Resource<ActivityDetailModel>> =
        activityRefreshSignalFlow.flatMapLatest { createActivityDetailFlow() }

    private fun createActivityDetailFlow(): Flow<Resource<ActivityDetailModel>> = flow {
        emit(Resource.Loading())
        val activity = activityRepository.getActivity(savedStateHandle.getActivityId())
        if (activity == null) {
            emit(Resource.Error(ActivityNotFoundException()))
        } else {
            val isMapVisible =
                activity.athleteInfo.userId == userAuthState.requireUserAccountId() ||
                    userPrefRepository.getShowActivityMapOnFeed(activity.athleteInfo.userId)
            emit(Resource.Success(ActivityDetailModel(activity, isMapVisible)))
        }
    }
        .flowOn(ioDispatcher)

    val screenStateFlow: Flow<ScreenState> = combine(
        activityDetailsFlow,
        currentUserPreferences.getMeasureSystemFlow()
    ) { activityResource, preferredSystem ->
        val activityData = activityResource.data?.activityData
        val userId = userAuthState.requireUserAccountId()
        val userPlaceIdentifier =
            userRecentActivityRepository.getRecentPlaceIdentifier(userId, useCache = false)
        val placeName =
            placeNameSelector.select(activityData?.placeIdentifier, userPlaceIdentifier)
        val locations = activityData?.id?.let { activityId ->
            activityRepository.getActivityLocationDataPoints(activityId)
        } ?: emptyList()
        ScreenState.create(
            activityResource,
            preferredSystem,
            activityDateTimeFormatter,
            placeName,
            activitySplitCalculator.createRunSplits(locations, preferredSystem)
        )
    }

    private fun SavedStateHandle.getActivityId(): String =
        get<String>(SAVED_STATE_ACTIVITY_ID)
            ?: ""

    fun refreshActivityDetails() {
        activityRefreshSignalFlow.value = !activityRefreshSignalFlow.value
    }

    class ActivityNotFoundException : Exception()

    sealed class ScreenState {
        object FullScreenLoading : ScreenState()
        object ErrorAndRetry : ScreenState()
        object UnknownState : ScreenState()

        class DataAvailable(
            val activityData: BaseActivityModel,
            val isMapVisible: Boolean,
            val preferredSystem: MeasureSystem,
            val activityPlaceName: String?,
            val activityFormattedStartTime: ActivityDateTimeFormatter.Result,
            val runSplits: List<Double>,
            val isStillLoading: Boolean,
        ) : ScreenState()

        companion object {
            fun create(
                activityDetailResource: Resource<ActivityDetailModel>,
                preferredSystem: MeasureSystem,
                activityDateTimeFormatter: ActivityDateTimeFormatter,
                activityPlaceName: String?,
                runSplits: List<Double>,
            ): ScreenState {
                val activityDetail = activityDetailResource.data
                return when {
                    activityDetail == null &&
                        activityDetailResource is Resource.Loading -> FullScreenLoading
                    activityDetail == null &&
                        activityDetailResource is Resource.Error -> ErrorAndRetry
                    activityDetail != null -> {
                        DataAvailable(
                            activityDetail.activityData,
                            activityDetail.isMapVisible,
                            preferredSystem,
                            activityPlaceName,
                            activityDateTimeFormatter.formatActivityDateTime(
                                activityDetail.activityData.startTime
                            ),
                            runSplits,
                            isStillLoading = activityDetailResource is Resource.Loading
                        )
                    }
                    else -> UnknownState
                }
            }
        }
    }

    class ActivityDetailModel(
        val activityData: BaseActivityModel,
        val isMapVisible: Boolean,
    )

    companion object {
        private const val SAVED_STATE_ACTIVITY_ID = "SAVED_STATE_ACTIVITY_ID"

        fun setInitialSavedState(handle: SavedStateHandle, activityId: String): SavedStateHandle {
            handle[SAVED_STATE_ACTIVITY_ID] = activityId
            return handle
        }
    }
}
