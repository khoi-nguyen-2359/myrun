package akio.apps.myrun.feature.userprofile.usecase

import akio.apps._base.data.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.feature.userprofile.GetProviderTokensUsecase
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class GetProviderTokensUsecaseImpl @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState
) : GetProviderTokensUsecase {

    @ExperimentalCoroutinesApi
    override fun getProviderTokens(): LiveData<Resource<ExternalProviders>> {
        val userId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        return externalAppProvidersRepository.getExternalProvidersFlow(userId)
            .asLiveData(timeoutInMs = 0)
    }
}