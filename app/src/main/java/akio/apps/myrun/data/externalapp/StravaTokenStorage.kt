package akio.apps.myrun.data.externalapp

import akio.apps.myrun.data.externalapp.model.ExternalAppToken

interface StravaTokenStorage {
    fun setToken(token: ExternalAppToken.StravaToken?)
    fun getToken(): ExternalAppToken.StravaToken?
    fun clear()
}