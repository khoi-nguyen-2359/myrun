package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.domain.activity.RunSplitsCalculator
import akio.apps.myrun.domain.user.PlaceNameSelector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ActivityDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val placeNameSelector: PlaceNameSelector,
    private val runSplitsCalculator: RunSplitsCalculator,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val activityDetailsMutableStateFlow: MutableStateFlow<Resource<BaseActivityModel>> =
        MutableStateFlow(Resource.Loading())

    val screenStateFlow: Flow<ScreenState> =
        activityDetailsMutableStateFlow.map(::combineScreenState).flowOn(ioDispatcher)

    private suspend fun combineScreenState(
        activityResource: Resource<BaseActivityModel>,
    ): ScreenState {
        val userId = userAuthenticationState.requireUserAccountId()
        val userPlaceIdentifier = userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
        val placeName = placeNameSelector.select(
            activityResource.data?.placeIdentifier,
            userPlaceIdentifier
        )
        val runSplits = if (activityResource is Resource.Success &&
            activityResource.data.activityType == ActivityType.Running
        ) {
            val locations =
                activityRepository.getActivityLocationDataPoints(activityResource.data.id)
            runSplitsCalculator.createRunSplits(locations)
        } else {
            emptyList()
        }
        return ScreenState.create(
            activityResource,
            activityDateTimeFormatter,
            placeName,
            runSplits
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
            val activityPlaceName: String?,
            val activityFormattedStartTime: ActivityDateTimeFormatter.Result,
            val runSplits: List<Double>,
            val isStillLoading: Boolean,
        ) : ScreenState()

        companion object {
            fun create(
                activityDetailResource: Resource<BaseActivityModel>,
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
