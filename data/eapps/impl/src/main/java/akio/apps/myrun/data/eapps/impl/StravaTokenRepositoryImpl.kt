package akio.apps.myrun.data.eapps.impl

import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.impl.mapper.StravaStravaTokenMapper
import javax.inject.Inject

const val STRAVA_APP_ID = "54817"
const val STRAVA_APP_SECRET = "805c1da4993b9439d583d4264809b50270ebae3a"

class StravaTokenRepositoryImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaTokenMapper: StravaStravaTokenMapper,
) : akio.apps.myrun.data.eapps.api.StravaTokenRepository {
    override suspend fun exchangeToken(code: String): ExternalAppToken.StravaToken {
        val stravaToken = stravaApi.exchangeToken(STRAVA_APP_ID, STRAVA_APP_SECRET, code)
        val tokenModel = stravaTokenMapper.map(stravaToken)
        return tokenModel
    }

    override suspend fun deauthorizeToken(accessToken: String) {
        val authToken = "Bearer $accessToken"
        stravaApi.deauthorize(authToken, accessToken)
    }
}