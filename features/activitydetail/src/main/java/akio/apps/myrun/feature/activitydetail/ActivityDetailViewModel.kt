package akio.apps.myrun.feature.activitydetail

import akio.apps.common.data.Resource
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.domain.recentplace.MakeActivityPlaceNameUsecase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ActivityDetailViewModel @Inject constructor(
    private val arguments: Arguments,
    private val activityRepository: ActivityRepository,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val makeActivityPlaceNameUsecase: MakeActivityPlaceNameUsecase,
) : ViewModel() {

    private val activityDetailsMutableStateFlow: MutableStateFlow<Resource<ActivityModel>> =
        MutableStateFlow(Resource.Loading())
    val activityDetails: Flow<Resource<ActivityModel>> = activityDetailsMutableStateFlow

    val activityPlaceName: Flow<String?> =
        activityDetailsMutableStateFlow.map { resource ->
            val userId = userAuthenticationState.getUserAccountId() ?: return@map null
            val activityPlaceIdentifier = resource.data?.placeIdentifier ?: return@map null
            val currentUserPlaceIdentifier =
                userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
            val placeName = makeActivityPlaceNameUsecase(
                activityPlaceIdentifier,
                currentUserPlaceIdentifier
            )
            placeName
        }

    init {
        loadActivityDetails()
    }

    fun loadActivityDetails() {
        viewModelScope.launch {
            activityDetailsMutableStateFlow.value = Resource.Loading()
            val activity = activityRepository.getActivity(arguments.activityId)
            if (activity == null) {
                activityDetailsMutableStateFlow.value = Resource.Error(ActivityNotFoundException())
            } else {
                activityDetailsMutableStateFlow.value = Resource.Success(activity)
            }
        }
    }

    class ActivityNotFoundException : Exception()

    data class Arguments(val activityId: String)
}
