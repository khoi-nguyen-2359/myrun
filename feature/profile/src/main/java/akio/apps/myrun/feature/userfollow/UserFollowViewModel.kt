package akio.apps.myrun.feature.userfollow

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowType
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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
                    isSelected = true
                ),
                TabState(
                    pagingDataFlow = createPagerFlow(UserFollowType.Follower),
                    isSelected = false
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
        .cachedIn(viewModelScope)

    private fun createPagingSource(userFollowType: UserFollowType): UserFollowPagingSource =
        UserFollowPagingSource(
            userFollowRepository,
            authenticationState,
            userFollowType,
            ioDispatcher
        )

    class ScreenState(val tabStates: List<TabState>)

    class TabState(
        val pagingDataFlow: Flow<PagingData<UserFollow>>,
        val isSelected: Boolean,
    )

    companion object {
        val INIT_SCREEN_STATE = ScreenState(tabStates = listOf())
    }
}
