package akio.apps.myrun.feature.userfollow

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowType
import akio.apps.myrun.feature.userfollow.model.FollowStatusTitle
import akio.apps.myrun.feature.userfollow.model.UserFollowListUiModel
import akio.apps.myrun.feature.userfollow.model.UserFollowUiModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class UserFollowViewModel @Inject constructor(
    private val userFollowRepository: UserFollowRepository,
    private val authenticationState: UserAuthenticationState,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val mapFollowTypeToPagingSource: MutableMap<UserFollowType, UserFollowPagingSource> =
        mutableMapOf()
    private val currentUserId: String by lazy { authenticationState.requireUserAccountId() }
    private val setLoadingRequestIds: Map<UserFollowType, MutableSet<String>> = mapOf(
        UserFollowType.Follower to mutableSetOf(),
        UserFollowType.Following to mutableSetOf()
    )
    private val mutableScreenState: MutableStateFlow<ScreenState> = MutableStateFlow(
        ScreenState(
            tabStates = listOf(
                TabState(
                    UserFollowType.Following,
                    pagingDataFlow = createPagerFlow(UserFollowType.Following)
                ),
                TabState(
                    UserFollowType.Follower,
                    pagingDataFlow = createPagerFlow(UserFollowType.Follower)
                )
            )
        )
    )
    val screenState: Flow<ScreenState> = mutableScreenState

    fun deleteFollowingRequest(uid: String) = viewModelScope.launch {
        modifyUserFollow(uid, UserFollowType.Following) {
            userFollowRepository.unfollowUser(currentUserId, uid)
        }
    }

    fun acceptFollowerRequest(uid: String) = viewModelScope.launch {
        modifyUserFollow(uid, UserFollowType.Follower) {
            userFollowRepository.acceptFollower(currentUserId, uid)
        }
    }

    fun deleteFollowerRequest(uid: String) = viewModelScope.launch {
        modifyUserFollow(uid, UserFollowType.Follower) {
            userFollowRepository.deleteFollower(currentUserId, uid)
        }
    }

    private suspend fun modifyUserFollow(
        uid: String,
        followType: UserFollowType,
        action: suspend () -> Unit,
    ) {
        try {
            setLoadingRequestIds[followType]?.add(uid)
            mapFollowTypeToPagingSource[followType]?.invalidate()
            withContext(ioDispatcher) { action() }
        } catch (ex: Exception) {
            Timber.e(ex)
        } finally {
            setLoadingRequestIds[followType]?.remove(uid)
            mapFollowTypeToPagingSource[followType]?.invalidate()
        }
    }

    private fun createPagerFlow(followType: UserFollowType) = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE
        )
    ) { createAndStorePagingSource(followType) }
        .flow
        .map { pagingData -> pagingData.mapToUiModel(followType).insertDividers() }
        .cachedIn(viewModelScope)

    private fun PagingData<UserFollow>.mapToUiModel(
        followType: UserFollowType,
    ): PagingData<UserFollowListUiModel> = map {
        UserFollowUiModel(it, setLoadingRequestIds[followType]?.contains(it.uid) == true)
    }

    private fun PagingData<UserFollowListUiModel>.insertDividers():
        PagingData<UserFollowListUiModel> =
        insertSeparators { above: UserFollowListUiModel?, below: UserFollowListUiModel? ->
            when {
                // Add the section title item
                (above == null && below is UserFollowUiModel) ||
                    (
                        above is UserFollowUiModel &&
                            below is UserFollowUiModel &&
                            above.userFollow.status != below.userFollow.status
                        ) -> {
                    FollowStatusTitle(below.userFollow.status)
                }
                else -> null
            }
        }

    private fun createAndStorePagingSource(userFollowType: UserFollowType): UserFollowPagingSource =
        UserFollowPagingSource(
            userFollowRepository,
            authenticationState,
            userFollowType,
            ioDispatcher
        ).also {
            mapFollowTypeToPagingSource[userFollowType] = it
        }

    class ScreenState(val tabStates: List<TabState>)

    class TabState(
        val type: UserFollowType,
        val pagingDataFlow: Flow<PagingData<UserFollowListUiModel>>,
    )

    companion object {
        private const val PAGE_SIZE = 50
        val INIT_SCREEN_STATE = ScreenState(tabStates = listOf())
    }
}
