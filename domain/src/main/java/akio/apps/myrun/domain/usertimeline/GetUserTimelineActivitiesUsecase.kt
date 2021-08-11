package akio.apps.myrun.domain.usertimeline

import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import javax.inject.Inject

class GetUserTimelineActivitiesUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val userFollowRepository: UserFollowRepository
) {
    suspend fun getUserTimelineActivity(
        startAfter: Long,
        count: Int
    ): akio.apps.common.data.Resource<List<ActivityModel>> = try {
        val userAccountId = userAuthenticationState.requireUserAccountId()

        val userIds = userFollowRepository.getUserFollowings(userAccountId).toMutableList()
        userIds.add(userAccountId)

        val activities = activityRepository.getActivitiesByStartTime(userIds, startAfter, count)
        akio.apps.common.data.Resource.Success(activities)
    } catch (ex: Exception) {
        akio.apps.common.data.Resource.Error(ex)
    }
}
