package akio.apps.myrun.data.externalapp

import akio.apps.myrun.data.externalapp.model.ExternalAppToken

interface StravaTokenRepository {
    suspend fun exchangeToken(code: String): ExternalAppToken.StravaToken
    suspend fun deauthorizeToken(accessToken: String)
}