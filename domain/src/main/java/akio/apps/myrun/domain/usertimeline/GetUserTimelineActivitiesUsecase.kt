package akio.apps.myrun.domain.usertimeline

import akio.apps._base.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userfollow.UserFollowRepository
import javax.inject.Inject

class GetUserTimelineActivitiesUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val userFollowRepository: UserFollowRepository
) {
    suspend fun getUserTimelineActivity(
        startAfter: Long,
        count: Int
    ): Resource<List<ActivityModel>> {
        val userAccountId = userAuthenticationState.getUserAccountId()
            ?: return Resource.Error(UnauthorizedUserError())

        val userIds = userFollowRepository.getUserFollowings(userAccountId).toMutableList()
        userIds.add(userAccountId)

        val activities = activityRepository.getActivitiesByStartTime(userIds, startAfter, count)
        return Resource.Success(activities)
    }
}
