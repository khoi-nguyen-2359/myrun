package akio.apps.myrun.feature.userfollow

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class UserFollowPagingSource(
    private val userFollowRepository: UserFollowRepository,
    private val authenticationState: UserAuthenticationState,
    private val userFollowType: UserFollowType,
    private val ioDispatcher: CoroutineDispatcher,
) : PagingSource<String, UserFollow>() {
    private val userId: String = authenticationState.requireUserAccountId()
    override suspend fun load(params: LoadParams<String>): LoadResult<String, UserFollow> {
        Timber.d("user follow page load, paramKey=${params.key} startAfter=$params.key")
        return try {
            val pageData = withContext(ioDispatcher) {
                userFollowRepository.getUserFollows(userId,
                    userFollowType,
                    params.key,
                    params.loadSize)
            }
            LoadResult.Page(
                data = pageData,
                prevKey = null,
                nextKey = pageData.lastOrNull()?.uid
            )
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }

    override fun getRefreshKey(state: PagingState<String, UserFollow>): String? = null
}
