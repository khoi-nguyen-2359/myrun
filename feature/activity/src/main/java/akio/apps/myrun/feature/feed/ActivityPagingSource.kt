package akio.apps.myrun.feature.feed

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.domain.activity.GetFeedActivitiesUsecase
import androidx.paging.PagingSource
import androidx.paging.PagingState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

class ActivityPagingSource(
    private val getFeedActivitiesUsecase: GetFeedActivitiesUsecase,
    private val ioDispatcher: CoroutineDispatcher,
) : PagingSource<Long, BaseActivityModel>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, BaseActivityModel> {
        val startAfter = params.key ?: System.currentTimeMillis()
        val resource = withContext(ioDispatcher) {
            getFeedActivitiesUsecase.getUserTimelineActivity(startAfter, params.loadSize)
        }
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

class ActivityPagingSourceFactory @Inject constructor(
    private val getFeedActivitiesUsecase: GetFeedActivitiesUsecase,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {
    fun createPagingSource(): ActivityPagingSource =
        ActivityPagingSource(getFeedActivitiesUsecase, ioDispatcher)
}
