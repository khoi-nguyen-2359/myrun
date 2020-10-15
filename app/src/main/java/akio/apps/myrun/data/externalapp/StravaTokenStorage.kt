package akio.apps.myrun.data.externalapp

import akio.apps.myrun.data.externalapp.model.ExternalAppToken

interface StravaTokenStorage {
    suspend fun setToken(token: ExternalAppToken.StravaToken?)
    suspend fun getToken(): ExternalAppToken.StravaToken?
    fun clear()
}