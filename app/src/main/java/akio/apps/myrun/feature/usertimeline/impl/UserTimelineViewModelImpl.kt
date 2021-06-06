package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.recentplace.PlaceIdentifier
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.domain.recentplace.CreateActivityDisplayPlaceNameUsecase
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class UserTimelineViewModelImpl @Inject constructor(
    private val activityPagingSource: ActivityPagingSource,
    private val createActivityDisplayPlaceNameUsecase: CreateActivityDisplayPlaceNameUsecase,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState
) : UserTimelineViewModel() {

    override val myActivityList: Flow<PagingData<Activity>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = 10
        ),
        initialKey = System.currentTimeMillis()
    ) { activityPagingSource }.flow

    private lateinit var currentUserPlaceIdentifier: CurrentUserPlaceIdentifier
    private suspend fun initUserRecentPlaceIdentifier(): CurrentUserPlaceIdentifier {
        if (!::currentUserPlaceIdentifier.isInitialized) {
            val userId = userAuthenticationState.getUserAccountId()
            val identifier = if (userId != null) {
                userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
            } else {
                null
            }
            currentUserPlaceIdentifier = CurrentUserPlaceIdentifier(identifier)
        }
        return currentUserPlaceIdentifier
    }

    override suspend fun getActivityDisplayPlaceName(activity: Activity): String? {
        val activityPlaceIdentifier = activity.placeIdentifier
        val userRecentPlaceIdentifier = initUserRecentPlaceIdentifier()
        return createActivityDisplayPlaceNameUsecase(
            activityPlaceIdentifier,
            userRecentPlaceIdentifier.identifier
        )
    }

    private class CurrentUserPlaceIdentifier(val identifier: PlaceIdentifier?)

    companion object {
        const val PAGE_SIZE = 4
    }
}
