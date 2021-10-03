package akio.apps.myrun.feature.configurator.viewmodel

import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.user.impl.GetUserProfileUsecase
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class UserAuthenticationViewModel @Inject constructor(
    private val getUserProfileUsecase: GetUserProfileUsecase,
) : ViewModel() {
    val userProfileFlow: Flow<UserProfile> =
        getUserProfileUsecase.getUserProfileFlow().mapNotNull { it.data }
}
