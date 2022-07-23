package akio.apps.myrun.data.eapps.impl

import akio.apps.myrun.data.eapps.api.StravaTokenRepository
import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.di.ExternalAppDataScope
import akio.apps.myrun.data.eapps.impl.mapper.StravaStravaTokenMapper
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// TODO: Config debug/release strava app
private const val STRAVA_APP_ID = "54817"
private const val STRAVA_APP_SECRET = "805c1da4993b9439d583d4264809b50270ebae3a"

@Singleton
@ContributesBinding(ExternalAppDataScope::class)
class StravaTokenRepositoryImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaTokenMapper: StravaStravaTokenMapper,
) : StravaTokenRepository {

    override suspend fun exchangeToken(code: String): ExternalAppToken.StravaToken {
        val stravaToken = stravaApi.exchangeToken(STRAVA_APP_ID, STRAVA_APP_SECRET, code)
        return stravaTokenMapper.map(stravaToken)
    }

    override suspend fun deauthorizeToken(accessToken: String) {
        val authToken = "Bearer $accessToken"
        stravaApi.deauthorize(authToken, accessToken)
    }
}
