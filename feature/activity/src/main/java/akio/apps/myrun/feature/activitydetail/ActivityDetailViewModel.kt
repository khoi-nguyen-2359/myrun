package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.CurrentUserPreferences
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
    private val userAuthenticationState: UserAuthenticationState,
    private val placeNameSelector: PlaceNameSelector,
    private val activitySplitCalculator: ActivitySplitCalculator,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
    currentUserPreferences: CurrentUserPreferences,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {

    private val activityRefreshSignalFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val activityDetailsFlow: Flow<Resource<BaseActivityModel>> =
        activityRefreshSignalFlow.flatMapLatest { createActivityDetailFlow() }

    private fun createActivityDetailFlow() = flow {
        emit(Resource.Loading())
        val activity = activityRepository.getActivity(savedStateHandle.getActivityId())
        if (activity == null) {
            emit(Resource.Error(ActivityNotFoundException()))
        } else {
            emit(Resource.Success(activity))
        }
    }
        .flowOn(ioDispatcher)

    val screenStateFlow: Flow<ScreenState> = combine(
        activityDetailsFlow,
        currentUserPreferences.getMeasureSystemFlow()
    ) { activityResource, preferredSystem ->
        val userId = userAuthenticationState.requireUserAccountId()
        val userPlaceIdentifier =
            userRecentActivityRepository.getRecentPlaceIdentifier(userId, useCache = false)
        val placeName =
            placeNameSelector.select(activityResource.data?.placeIdentifier, userPlaceIdentifier)
        val locations = activityResource.data?.id?.let { activityId ->
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
            val preferredSystem: MeasureSystem,
            val activityPlaceName: String?,
            val activityFormattedStartTime: ActivityDateTimeFormatter.Result,
            val runSplits: List<Double>,
            val isStillLoading: Boolean,
        ) : ScreenState()

        companion object {
            fun create(
                activityDetailResource: Resource<BaseActivityModel>,
                preferredSystem: MeasureSystem,
                activityDateTimeFormatter: ActivityDateTimeFormatter,
                activityPlaceName: String?,
                runSplits: List<Double>,
            ): ScreenState {
                val activityData = activityDetailResource.data
                return when {
                    activityData == null &&
                        activityDetailResource is Resource.Loading -> FullScreenLoading
                    activityData == null &&
                        activityDetailResource is Resource.Error -> ErrorAndRetry
                    activityData != null -> {
                        DataAvailable(
                            activityData,
                            preferredSystem,
                            activityPlaceName,
                            activityDateTimeFormatter.formatActivityDateTime(
                                activityData.startTime
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

    companion object {
        private const val SAVED_STATE_ACTIVITY_ID = "SAVED_STATE_ACTIVITY_ID"

        fun setInitialSavedState(handle: SavedStateHandle, activityId: String): SavedStateHandle {
            handle[SAVED_STATE_ACTIVITY_ID] = activityId
            return handle
        }
    }
}
