package akio.apps.myrun.data.externalapp.impl

import akio.apps.myrun.data.externalapp.StravaTokenRepository
import akio.apps.myrun.data.externalapp.mapper.StravaStravaTokenMapper
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import javax.inject.Inject

const val STRAVA_APP_ID = "54817"
const val STRAVA_APP_SECRET = "805c1da4993b9439d583d4264809b50270ebae3a"

class StravaTokenRepositoryImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaTokenMapper: StravaStravaTokenMapper
) : StravaTokenRepository {
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
