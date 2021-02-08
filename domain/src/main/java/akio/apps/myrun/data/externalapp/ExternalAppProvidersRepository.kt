package akio.apps.myrun.data.externalapp

import akio.apps._base.Resource
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import kotlinx.coroutines.flow.Flow

interface ExternalAppProvidersRepository {
    suspend fun updateStravaProvider(accountId: String, token: ExternalAppToken.StravaToken)
    suspend fun removeStravaProvider(accountId: String)
    suspend fun getStravaProviderToken(accountId: String): ExternalAppToken.StravaToken?
    fun getExternalProvidersFlow(accountId: String): Flow<Resource<ExternalProviders>>
    suspend fun getExternalProviders(accountId: String): ExternalProviders
}
