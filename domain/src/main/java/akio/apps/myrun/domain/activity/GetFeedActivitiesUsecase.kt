package akio.apps.myrun.domain.activity

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.UserFollowRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetFeedActivitiesUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val userFollowRepository: UserFollowRepository,
) {
    suspend fun getUserTimelineActivity(
        startAfter: Long,
        count: Int,
    ): Resource<List<BaseActivityModel>> = try {
        val userAccountId = userAuthenticationState.requireUserAccountId()

        // get first() may return cached data
        val userIds = userFollowRepository.getUserFollowingsFlow(userAccountId).first()
            .map { it.uid } + listOf(userAccountId)

        val activities =
            activityRepository.getActivitiesByStartTime(userAccountId, userIds, startAfter, count)
        Resource.Success(activities)
    } catch (ex: Exception) {
        Resource.Error(ex)
    }
}
