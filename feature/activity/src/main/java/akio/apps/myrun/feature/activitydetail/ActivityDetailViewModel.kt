package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.domain.activity.ActivitySplitCalculator
import akio.apps.myrun.domain.user.PlaceNameSelector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ActivityDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val userRecentActivityRepository: UserRecentActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val placeNameSelector: PlaceNameSelector,
    private val activitySplitCalculator: ActivitySplitCalculator,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
    userPreferences: UserPreferences,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val activityDetailsMutableStateFlow: MutableStateFlow<Resource<BaseActivityModel>> =
        MutableStateFlow(Resource.Loading())

    val screenStateFlow: Flow<ScreenState> = combine(
        activityDetailsMutableStateFlow,
        userPreferences.getMeasureSystemFlow()
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

    init {
        loadActivityDetails()
    }

    fun loadActivityDetails() {
        viewModelScope.launch {
            activityDetailsMutableStateFlow.value = Resource.Loading()
            val activity = withContext(ioDispatcher) {
                activityRepository.getActivity(savedStateHandle.getActivityId())
            }
            if (activity == null) {
                activityDetailsMutableStateFlow.value = Resource.Error(ActivityNotFoundException())
            } else {
                activityDetailsMutableStateFlow.value = Resource.Success(activity)
            }
        }
    }

    private fun SavedStateHandle.getActivityId(): String =
        get<String>(SAVED_STATE_ACTIVITY_ID)
            ?: ""

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
