package akio.apps.myrun.feature.home.feed

import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.time.Now
import akio.apps.myrun.domain.activity.impl.GetFeedActivitiesUsecase
import androidx.paging.PagingSource
import androidx.paging.PagingState
import javax.inject.Inject
import timber.log.Timber

class ActivityPagingSource @Inject constructor(
    private val getFeedActivitiesUsecase: GetFeedActivitiesUsecase,
) : PagingSource<Long, ActivityModel>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, ActivityModel> {
        val startAfter = params.key ?: Now.currentTimeMillis()
        val resource = getFeedActivitiesUsecase.getUserTimelineActivity(startAfter, params.loadSize)
        Timber.d("feed resource $resource")
        return when (resource) {
            is akio.apps.myrun.data.Resource.Success ->
                LoadResult.Page(
                    data = resource.data,
                    prevKey = null,
                    nextKey = resource.data.lastOrNull()?.startTime
                )
            is akio.apps.myrun.data.Resource.Error -> LoadResult.Error(resource.exception)
            else -> LoadResult.Error(Exception("Invalid timeline resource"))
        }
    }

    override fun getRefreshKey(state: PagingState<Long, ActivityModel>): Long? = null
}

class ActivityPagingSourceFactory @Inject constructor(
    private val getFeedActivitiesUsecase: GetFeedActivitiesUsecase,
) {
    operator fun invoke(): ActivityPagingSource =
        ActivityPagingSource(getFeedActivitiesUsecase)
}
