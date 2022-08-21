package akio.apps.myrun.feature.userfollow

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.UserFollowType
import akio.apps.myrun.feature.userfollow.model.FollowStatusDivider
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

class UserFollowViewModel @Inject constructor(
    private val userFollowRepository: UserFollowRepository,
    private val authenticationState: UserAuthenticationState,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val mutableScreenState: MutableStateFlow<ScreenState> = MutableStateFlow(
        ScreenState(
            tabStates = listOf(
                TabState(
                    pagingDataFlow = createPagerFlow(UserFollowType.Following),
                ),
                TabState(
                    pagingDataFlow = createPagerFlow(UserFollowType.Follower),
                ),
            )
        )
    )
    val screenState: Flow<ScreenState> = mutableScreenState

    private fun createPagerFlow(followType: UserFollowType) = Pager(
        config = PagingConfig(
            pageSize = 100,
            enablePlaceholders = false,
            prefetchDistance = 100
        )
    ) { createPagingSource(followType) }
        .flow
        .map { pagingData ->
            pagingData.map { UserFollowUiModel(it) }.insertDividers()
        }
        .cachedIn(viewModelScope)

    private fun PagingData<out UserFollowListUiModel>.insertDividers(): PagingData<UserFollowListUiModel> =
        insertSeparators { i1: UserFollowListUiModel?, i2: UserFollowListUiModel? ->
            if (i1 is UserFollowUiModel && i2 is UserFollowUiModel &&
                i1.userFollow.status != i2.userFollow.status
            ) {
                FollowStatusDivider
            } else {
                null
            }
        }

    private fun createPagingSource(userFollowType: UserFollowType): UserFollowPagingSource =
        UserFollowPagingSource(
            userFollowRepository,
            authenticationState,
            userFollowType,
            ioDispatcher
        )

    class ScreenState(val tabStates: List<TabState>)

    class TabState(
        val pagingDataFlow: Flow<PagingData<UserFollowListUiModel>>,
    )

    companion object {
        val INIT_SCREEN_STATE = ScreenState(tabStates = listOf())
    }
}
