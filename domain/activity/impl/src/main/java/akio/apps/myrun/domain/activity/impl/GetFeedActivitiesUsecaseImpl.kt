package akio.apps.myrun.domain.activity.impl

import akio.apps.myrun.data.Resource
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.domain.activity.api.ActivityRepository
import akio.apps.myrun.domain.activity.api.GetFeedActivitiesUsecase
import akio.apps.myrun.domain.activity.api.model.ActivityModel
import javax.inject.Inject

class GetFeedActivitiesUsecaseImpl @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val userFollowRepository: UserFollowRepository,
) : GetFeedActivitiesUsecase {
    override suspend fun getUserTimelineActivity(
        startAfter: Long,
        count: Int,
    ): Resource<List<ActivityModel>> = try {
        val userAccountId = userAuthenticationState.requireUserAccountId()

        val userIds = userFollowRepository.getUserFollowings(userAccountId).toMutableList()
        userIds.add(userAccountId)

        val activities =
            activityRepository.getActivitiesByStartTime(userAccountId, userIds, startAfter, count)
        Resource.Success(activities)
    } catch (ex: Exception) {
        Resource.Error(ex)
    }
}
