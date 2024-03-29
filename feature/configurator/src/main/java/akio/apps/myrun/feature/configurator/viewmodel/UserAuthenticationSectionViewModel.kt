package akio.apps.myrun.feature.configurator.viewmodel

import akio.apps.myrun.base.di.NamedIoDispatcher
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull

class UserAuthenticationSectionViewModel @Inject constructor(
    getUserProfileUsecase: GetUserProfileUsecase,
    userAuthState: UserAuthenticationState,
    @NamedIoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val userId: String = userAuthState.requireUserAccountId()
    val userProfileFlow: Flow<UserProfile> =
        getUserProfileUsecase.getUserProfileFlow(userId).mapNotNull { it.data }.flowOn(ioDispatcher)
}
