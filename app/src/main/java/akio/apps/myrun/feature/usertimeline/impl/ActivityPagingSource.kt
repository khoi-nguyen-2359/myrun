package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.common.data.Resource
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.domain.usertimeline.GetUserTimelineActivitiesUsecase
import androidx.paging.PagingSource
import androidx.paging.PagingState
import javax.inject.Inject
import timber.log.Timber

class ActivityPagingSource @Inject constructor(
    private val getUserTimelineActivitiesUsecase: GetUserTimelineActivitiesUsecase,
) : PagingSource<Long, ActivityModel>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, ActivityModel> {
        val startAfter = params.key ?: System.currentTimeMillis()
        val resource =
            getUserTimelineActivitiesUsecase.getUserTimelineActivity(startAfter, params.loadSize)
        Timber.d("feed resource $resource")
        return when (resource) {
            is Resource.Success ->
                LoadResult.Page(
                    data = resource.data,
                    prevKey = null,
                    nextKey = resource.data.lastOrNull()?.startTime
                )
            is Resource.Error -> LoadResult.Error(resource.exception)
            else -> LoadResult.Error(Exception("Invalid timeline resource"))
        }
    }

    override fun getRefreshKey(state: PagingState<Long, ActivityModel>): Long? = null
}

class ActivityPagingSourceFactory @Inject constructor(
    private val getUserTimelineActivitiesUsecase: GetUserTimelineActivitiesUsecase,
) {
    operator fun invoke(): ActivityPagingSource =
        ActivityPagingSource(getUserTimelineActivitiesUsecase)
}
