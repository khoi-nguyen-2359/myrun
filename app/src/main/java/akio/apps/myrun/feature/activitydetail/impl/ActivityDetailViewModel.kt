package akio.apps.myrun.feature.activitydetail.impl

import akio.apps.common.data.Resource
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.domain.recentplace.MakeActivityPlaceNameUsecase
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ActivityDetailViewModel @Inject constructor(
    private val params: Params,
    private val activityRepository: ActivityRepository,
    private val activityModelMapper: ActivityModelMapper,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val makeActivityPlaceNameUsecase: MakeActivityPlaceNameUsecase,
) : ViewModel() {

    private val activityDetailsMutableStateFlow: MutableStateFlow<Resource<Activity>> =
        MutableStateFlow(Resource.Loading())
    val activityDetails: Flow<Resource<Activity>> = activityDetailsMutableStateFlow

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

    fun loadActivityDetails() {
        viewModelScope.launch {
            activityDetailsMutableStateFlow.value = Resource.Loading()
            val activity = activityRepository.getActivity(params.activityId)
                ?.let(activityModelMapper::map)
            if (activity == null) {
                activityDetailsMutableStateFlow.value = Resource.Error(ActivityNotFoundException())
            } else {
                activityDetailsMutableStateFlow.value = Resource.Success(activity)
            }
        }
    }

    class ActivityNotFoundException : Exception()

    data class Params(val activityId: String)
}
