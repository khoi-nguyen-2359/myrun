package akio.apps.myrun.data.externalapp.impl

import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import android.content.SharedPreferences
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Named

class StravaTokenStorageImpl @Inject constructor(
    private @Named(ExternalAppDataModule.NAME_STRAVA_TOKEN_PREFERENCES) val prefs: SharedPreferences,
) : StravaTokenStorage {

    private val gson = Gson()

    override fun setToken(token: ExternalAppToken.StravaToken?) {
        val jsonToken = token?.let { gson.toJson(it) }
        prefs.edit().putString(KEY_STRAVA_TOKEN, jsonToken).apply()
    }

    override fun getToken(): ExternalAppToken.StravaToken? {
        val jsonToken = prefs.getString(KEY_STRAVA_TOKEN, null)
        return jsonToken?.let {
            gson.fromJson(it, ExternalAppToken.StravaToken::class.java)
        }
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        const val KEY_STRAVA_TOKEN = "KEY_STRAVA_TOKEN"
    }
}