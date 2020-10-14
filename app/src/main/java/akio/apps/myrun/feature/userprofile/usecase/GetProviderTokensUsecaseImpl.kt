package akio.apps.myrun.feature.userprofile.usecase

import akio.apps._base.data.Resource
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.feature.userprofile.GetProviderTokensUsecase
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class GetProviderTokensUsecaseImpl @Inject constructor(
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState
) : GetProviderTokensUsecase {

    @ExperimentalCoroutinesApi
    override fun getProviderTokens(): LiveData<Resource<ExternalProviders>> =
        userAuthenticationState.getUserAccountFlow().flatMapLatest { account ->
            externalAppProvidersRepository.getExternalProvidersFlow(account.uid)
        }.asLiveData(timeoutInMs = 0)
}