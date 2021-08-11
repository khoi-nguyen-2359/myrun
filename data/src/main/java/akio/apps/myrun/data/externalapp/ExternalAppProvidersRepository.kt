package akio.apps.myrun.data.externalapp

import akio.apps.common.data.Resource
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import kotlinx.coroutines.flow.Flow

interface ExternalAppProvidersRepository {
    suspend fun updateStravaProvider(accountId: String, token: ExternalAppToken.StravaToken)
    suspend fun removeStravaProvider(accountId: String)
    suspend fun getStravaProviderToken(accountId: String): ExternalAppToken.StravaToken?

    /**
     * Gets the flag of strava syncing status. This flag works in offline mode because is it
     * fetched and stored together with strava token data.
     */
    suspend fun isStravaSyncEnabled(): Boolean
    fun getExternalProvidersFlow(accountId: String): Flow<Resource<ExternalProviders>>
    suspend fun getExternalProviders(accountId: String): ExternalProviders
}
