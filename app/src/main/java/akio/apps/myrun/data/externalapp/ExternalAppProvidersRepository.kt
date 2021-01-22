package akio.apps.myrun.data.externalapp

import akio.apps._base.data.Resource
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import kotlinx.coroutines.flow.Flow

interface ExternalAppProvidersRepository {
    fun updateStravaProvider(accountId: String, token: ExternalAppToken.StravaToken)
    fun removeStravaProvider(accountId: String)
    fun getExternalProvidersFlow(accountId: String): Flow<Resource<ExternalProviders>>
    suspend fun getExternalProviders(accountId: String): ExternalProviders
}
