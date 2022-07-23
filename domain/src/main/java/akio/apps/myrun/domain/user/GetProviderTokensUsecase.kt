package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaSyncState
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach

class GetProviderTokensUsecase @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val stravaSyncState: StravaSyncState,
    private val userAuthenticationState: UserAuthenticationState,
) {
    fun getProviderTokensFlow(): Flow<Resource<out ExternalProviders>> = try {
        val accountId = userAuthenticationState.requireUserAccountId()
        externalAppProvidersRepository.getExternalProvidersFlow(accountId)
            .onEach {
                if (it.data?.strava == null) {
                    stravaSyncState.setStravaSyncAccountId(null)
                } else {
                    stravaSyncState.setStravaSyncAccountId(accountId)
                }
            }
    } catch (ex: Exception) {
        flowOf(Resource.Error(ex))
    }
}
