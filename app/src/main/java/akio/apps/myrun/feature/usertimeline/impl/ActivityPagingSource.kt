package akio.apps.myrun.feature.usertimeline.impl

import akio.apps._base.Resource
import akio.apps.myrun.domain.usertimeline.GetUserTimelineActivitiesUsecase
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
import androidx.paging.PagingSource
import androidx.paging.PagingState
import javax.inject.Inject

class ActivityPagingSource @Inject constructor(
    private val getUserTimelineActivitiesUsecase: GetUserTimelineActivitiesUsecase,
    private val activityModelMapper: ActivityModelMapper
) : PagingSource<Long, Activity>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Activity> {
        val startAfter = params.key ?: System.currentTimeMillis()
        val resource =
            getUserTimelineActivitiesUsecase.getUserTimelineActivity(startAfter, params.loadSize)
        return when (resource) {
            is Resource.Success ->
                LoadResult.Page(
                    data = resource.data.map(activityModelMapper::map),
                    prevKey = null,
                    nextKey = resource.data.lastOrNull()?.startTime
                )
            is Resource.Error -> LoadResult.Error(resource.exception)
            else -> LoadResult.Error(Exception("Invalid timeline resource"))
        }
    }

    override fun getRefreshKey(state: PagingState<Long, Activity>): Long? = null
}

class ActivityPagingSourceFactory @Inject constructor(
    private val getUserTimelineActivitiesUsecase: GetUserTimelineActivitiesUsecase,
    private val activityModelMapper: ActivityModelMapper
) {
    operator fun invoke(): ActivityPagingSource =
        ActivityPagingSource(getUserTimelineActivitiesUsecase, activityModelMapper)
}
