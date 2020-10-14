package akio.apps.myrun.feature.editprofile.impl

import akio.apps.myrun.STRAVA_APP_ID
import akio.apps.myrun.STRAVA_APP_SECRET
import akio.apps.myrun.data.externalapp.StravaApi
import akio.apps.myrun.feature.editprofile.ExchangeStravaLoginCodeUsecase
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.mapper.StravaTokenEntityMapper
import javax.inject.Inject

class ExchangeStravaLoginCodeUsecaseImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaTokenMapper: StravaTokenEntityMapper
): ExchangeStravaLoginCodeUsecase {
    override suspend fun exchangeStravaLoginCode(code: String): ExternalAppToken.StravaToken {
        val stravaToken = stravaApi.exchangeToken(STRAVA_APP_ID, STRAVA_APP_SECRET, code)
        val tokenModel = stravaTokenMapper.map(stravaToken)
        return tokenModel
    }
}