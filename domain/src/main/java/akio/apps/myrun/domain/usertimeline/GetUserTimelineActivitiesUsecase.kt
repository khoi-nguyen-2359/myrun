package akio.apps.myrun.domain.usertimeline

import akio.apps.common.data.Resource
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityModel
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
    ): Resource<List<ActivityModel>> = try {
        val userAccountId = userAuthenticationState.requireUserAccountId()

        val userIds = userFollowRepository.getUserFollowings(userAccountId).toMutableList()
        userIds.add(userAccountId)

        val activities = activityRepository.getActivitiesByStartTime(userIds, startAfter, count)
        Resource.Success(activities)
    } catch (ex: Exception) {
        Resource.Error(ex)
    }
}
