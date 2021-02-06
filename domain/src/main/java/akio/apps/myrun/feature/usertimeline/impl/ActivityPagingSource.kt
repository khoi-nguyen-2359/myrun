package akio.apps.myrun.feature.usertimeline.impl

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userfollow.UserFollowRepository
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityEntityMapper
import androidx.paging.PagingSource
import javax.inject.Inject

class ActivityPagingSource @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val activityEntityMapper: ActivityEntityMapper,
    private val userAuthenticationState: UserAuthenticationState,
    private val userFollowRepository: UserFollowRepository
) : PagingSource<Long, Activity>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Activity> {
        val userAccountId = userAuthenticationState.getUserAccountId()
            ?: return LoadResult.Error(UnauthorizedUserError())

        val userIds = userFollowRepository.getUserFollowings(userAccountId)
            .toMutableList()
        userIds.add(userAccountId)

        val startAfter = params.key ?: System.currentTimeMillis()
        val pageData =
            activityRepository.getActivitiesByStartTime(userIds, startAfter, params.loadSize)
        return LoadResult.Page(
            data = pageData.map(activityEntityMapper::map),
            prevKey = null,
            nextKey = pageData.lastOrNull()?.startTime
        )
    }
}
