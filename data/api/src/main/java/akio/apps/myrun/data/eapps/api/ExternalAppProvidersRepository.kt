package akio.apps.myrun.data.eapps.api

import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import kotlinx.coroutines.flow.Flow

interface ExternalAppProvidersRepository {
    suspend fun updateStravaProvider(accountId: String, token: ExternalAppToken.StravaToken)
    suspend fun removeStravaProvider(accountId: String)
    suspend fun getStravaProviderToken(accountId: String): ExternalAppToken.StravaToken?
    fun getExternalProvidersFlow(accountId: String): Flow<Resource<out ExternalProviders>>
}
