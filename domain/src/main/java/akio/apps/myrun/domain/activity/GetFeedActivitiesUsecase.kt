package akio.apps.myrun.domain.activity

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.FollowStatus
import javax.inject.Inject

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
        val userFollowings = userFollowRepository.getCachedUserFollowings(userAccountId)
            ?: userFollowRepository.getUserFollowings(userAccountId)
        val userIds = buildList {
            val followingUids = userFollowings.mapNotNull { userFollow ->
                if (userFollow.status == FollowStatus.Accepted) {
                    userFollow.uid
                } else {
                    null
                }
            }
            add(userAccountId)
            addAll(followingUids)
        }

        val activities = activityRepository.getActivitiesByStartTime(userIds, startAfter, count)
        Resource.Success(activities)
    } catch (ex: Exception) {
        Resource.Error(ex)
    }
}
