package akio.apps.myrun.data.eapps.api

import akio.apps.myrun.data.eapps.api.model.ExternalAppToken

interface StravaTokenRepository {
    suspend fun exchangeToken(code: String): ExternalAppToken.StravaToken
    suspend fun deauthorizeToken(accessToken: String)
}
