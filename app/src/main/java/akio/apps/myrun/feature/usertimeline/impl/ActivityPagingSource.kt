package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.feature.usertimeline.Activity
import akio.apps.myrun.feature.usertimeline.ActivityEntityMapper
import androidx.paging.PagingSource
import javax.inject.Inject

class ActivityPagingSource @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val activityEntityMapper: ActivityEntityMapper
): PagingSource<Long, Activity>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Activity> {
        val startAfter = params.key ?: System.currentTimeMillis()
        val pageData = activityRepository.getActivitiesByStartTime(startAfter, params.loadSize)
        return LoadResult.Page(
            data = pageData.map(activityEntityMapper::map),
            prevKey = null,
            nextKey = pageData.lastOrNull()?.startTime
        )
    }
}