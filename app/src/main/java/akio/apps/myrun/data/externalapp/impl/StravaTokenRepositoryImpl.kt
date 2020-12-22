package akio.apps.myrun.data.externalapp.impl

import akio.apps.myrun.data.externalapp.StravaTokenRepository
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule.Companion.STRAVA_APP_ID
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule.Companion.STRAVA_APP_SECRET
import akio.apps.myrun.data.externalapp.mapper.StravaTokenEntityMapper
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import javax.inject.Inject

class StravaTokenRepositoryImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaTokenEntityMapper: StravaTokenEntityMapper
) : StravaTokenRepository {
    override suspend fun exchangeToken(code: String): ExternalAppToken.StravaToken {
        val stravaToken = stravaApi.exchangeToken(STRAVA_APP_ID, STRAVA_APP_SECRET, code)
        val tokenModel = stravaTokenEntityMapper.map(stravaToken)
        return tokenModel
    }

    override suspend fun deauthorizeToken(accessToken: String) {
        val authToken = "Bearer $accessToken"
        stravaApi.deauthorize(authToken, accessToken)
    }
}