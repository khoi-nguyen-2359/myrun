package akio.apps.myrun.data.externalapp.impl

import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.data.externalapp.entity.StravaAthlete
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class StravaTokenStorageImpl @Inject constructor(
    appContext: Context
) : StravaTokenStorage {

    private val prefDataStore: DataStore<Preferences> = appContext.createDataStore("strava_token_storage")

    override suspend fun setToken(token: ExternalAppToken.StravaToken) {
        prefDataStore.edit {
            it[KEY_ACCESS_TOKEN] = token.accessToken
            it[KEY_REFRESH_TOKEN] = token.refreshToken
            it[KEY_ATHLETE_ID] = token.athlete.id
        }
    }

    override suspend fun getToken(): ExternalAppToken.StravaToken? {
        return prefDataStore.data.map {
            val accessToken = it[KEY_ACCESS_TOKEN]
            val refreshToken = it[KEY_REFRESH_TOKEN]
            val athleteId = it[KEY_ATHLETE_ID]
            if (accessToken == null || refreshToken == null || athleteId == null) {
                return@map null
            }

            return@map ExternalAppToken.StravaToken(accessToken, refreshToken, StravaAthlete(athleteId))
        }
            .first()
    }

    override suspend fun clear() {
        prefDataStore.edit { it.clear() }
    }

    companion object {
        private val KEY_ACCESS_TOKEN = preferencesKey<String>("KEY_ACCESS_TOKEN")
        private val KEY_REFRESH_TOKEN = preferencesKey<String>("KEY_REFRESH_TOKEN")
        private val KEY_ATHLETE_ID = preferencesKey<Long>("KEY_ATHLETE_ID")
    }
}