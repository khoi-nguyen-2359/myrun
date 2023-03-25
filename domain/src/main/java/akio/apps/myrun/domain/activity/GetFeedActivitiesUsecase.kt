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
    ): Resource<List<FeedActivityModel>> = try {
        val currentUserId = userAuthenticationState.requireUserAccountId()
        val userFollowingMap = userFollowRepository.getUserFollowings(
            currentUserId,
            useCache = true
        )
            .associateBy { it.uid }
        val userIds = userFollowingMap.values.mapNotNull { userFollow ->
            if (userFollow.status == FollowStatus.Accepted) {
                userFollow.uid
            } else {
                null
            }
        } + listOf(currentUserId)
        val activities =
            activityRepository.getActivitiesByStartTime(userIds, startAfter, count, useCache = true)
                .map { activityData ->
                    FeedActivityModel(
                        activityData,
                        userFollowingMap[activityData.athleteInfo.userId]?.isMapVisible ?: true
                    )
                }
        Resource.Success(activities)
    } catch (ex: Exception) {
        Resource.Error(ex)
    }

    data class FeedActivityModel(
        val activityData: BaseActivityModel,
        val isMapVisible: Boolean,
    )
}
