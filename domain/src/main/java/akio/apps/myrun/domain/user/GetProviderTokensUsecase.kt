package akio.apps.myrun.domain.user

import akio.apps._base.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetProviderTokensUsecase @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState
) {
    fun getProviderTokensFlow(): Flow<Resource<ExternalProviders>> {
        val userId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        return externalAppProvidersRepository.getExternalProvidersFlow(userId)
    }
}
