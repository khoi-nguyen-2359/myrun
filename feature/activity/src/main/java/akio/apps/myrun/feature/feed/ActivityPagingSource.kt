package akio.apps.myrun.feature.feed

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.domain.activity.GetFeedActivitiesUsecase
import androidx.paging.PagingSource
import androidx.paging.PagingState
import javax.inject.Inject
import timber.log.Timber

internal class ActivityPagingSource @Inject constructor(
    private val getFeedActivitiesUsecase: GetFeedActivitiesUsecase,
) : PagingSource<Long, BaseActivityModel>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, BaseActivityModel> {
        val startAfter = params.key ?: System.currentTimeMillis()
        val resource = getFeedActivitiesUsecase.getUserTimelineActivity(startAfter, params.loadSize)
        Timber.d("feed resource paramKey=${params.key} startAfter=$startAfter")
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

    override fun getRefreshKey(state: PagingState<Long, BaseActivityModel>): Long? = null
}

internal class ActivityPagingSourceFactory @Inject constructor(
    private val getFeedActivitiesUsecase: GetFeedActivitiesUsecase,
) {
    fun createPagingSource(): ActivityPagingSource =
        ActivityPagingSource(getFeedActivitiesUsecase)
}