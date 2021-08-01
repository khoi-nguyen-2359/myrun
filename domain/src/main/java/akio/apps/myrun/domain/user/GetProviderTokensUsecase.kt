package akio.apps.myrun.domain.user

import akio.apps._base.Resource
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetProviderTokensUsecase @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState
) {
    fun getProviderTokensFlow(): Flow<Resource<ExternalProviders>> = try {
        val userId = userAuthenticationState.requireUserAccountId()
        externalAppProvidersRepository.getExternalProvidersFlow(userId)
    } catch (ex: Exception) {
        flowOf(Resource.Error(ex))
    }
}
