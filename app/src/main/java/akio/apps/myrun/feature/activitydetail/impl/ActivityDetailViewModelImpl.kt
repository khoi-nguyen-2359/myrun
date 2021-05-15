package akio.apps.myrun.feature.activitydetail.impl

import akio.apps._base.Resource
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.recentplace.entity.PlaceIdentifier
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ActivityDetailViewModelImpl @Inject constructor(
    private val params: Params,
    private val activityRepository: ActivityRepository,
    private val activityModelMapper: ActivityModelMapper,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState
) : ActivityDetailViewModel() {

    private val activityDetailsMutableStateFlow: MutableStateFlow<Resource<Activity>> =
        MutableStateFlow(Resource.Loading())
    override val activityDetails: Flow<Resource<Activity>> = activityDetailsMutableStateFlow

    override fun getActivityFormattedDateTime(): ActivityDateTimeFormatter.Result {
        val activityDetailsData = activityDetailsMutableStateFlow.value.data
        return activityDateTimeFormatter.formatActivityDateTime(
            activityDetailsData?.startTime ?: 0L
        )
    }

    override suspend fun getActivityPlaceDisplayName(): String? {
        val userId = userAuthenticationState.getUserAccountId() ?: return null
        val placeIdentifier =
            activityDetailsMutableStateFlow.value.data?.placeIdentifier ?: return null
        val currentUserPlaceIdentifier =
            userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
        val activityPlaceIdentifier = PlaceIdentifier(placeIdentifier)
        val activityAddressList = activityPlaceIdentifier.toAddressNameList()
        if (activityAddressList.isEmpty()) {
            return null
        }
        val placeNameComponentList = if (activityAddressList.size <= 2) {
            // activity address length <= 2, take all items
            activityAddressList
        } else {
            // activity address length >= 3
            if (currentUserPlaceIdentifier == null) {
                activityAddressList.take(2)
            } else {
                val currentUserAddressList = currentUserPlaceIdentifier.toAddressNameList()
                var i = 0
                while (i < currentUserAddressList.size &&
                    i < activityAddressList.size &&
                    currentUserAddressList[i] == activityAddressList[i]
                ) {
                    ++i
                }
                if (i >= activityAddressList.size - 1) {
                    activityAddressList.takeLast(2)
                } else {
                    activityAddressList.subList(i, i + 2)
                }
            }
        }

        return placeNameComponentList.joinToString(", ")
    }

    override fun loadActivityDetails() {
        viewModelScope.launch {
            val activity = activityRepository.getActivity(params.activityId)
                ?.let(activityModelMapper::map)
            if (activity == null) {
                activityDetailsMutableStateFlow.value =
                    Resource.Error(ActivityNotFoundException())
            } else {
                activityDetailsMutableStateFlow.value = Resource.Success(activity)
            }
        }
    }
}
