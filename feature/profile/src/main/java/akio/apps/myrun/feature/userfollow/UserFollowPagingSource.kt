package akio.apps.myrun.feature.userfollow

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowPagingToken
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
) : PagingSource<UserFollowPagingToken, UserFollow>() {
    private val userId: String = authenticationState.requireUserAccountId()
    override suspend fun load(params: LoadParams<UserFollowPagingToken>): LoadResult<UserFollowPagingToken, UserFollow> {
        return try {
            val pageData = withContext(ioDispatcher) {
                userFollowRepository.getUserFollows(
                    userId,
                    userFollowType,
                    params.loadSize,
                    params.key,
                )
            }
            val nextKey = pageData.lastOrNull()
                ?.let { UserFollowPagingToken(it.uid, it.status, it.displayName) }
            Timber.d("$userFollowType loaded, startAfter=${params.key}, size=${pageData.size} nextKey=$nextKey")
            LoadResult.Page(pageData, prevKey = null, nextKey)
        } catch (ex: Exception) {
            Timber.e(ex)
            LoadResult.Error(ex)
        }
    }

    override fun getRefreshKey(state: PagingState<UserFollowPagingToken, UserFollow>): UserFollowPagingToken? =
        null
}
