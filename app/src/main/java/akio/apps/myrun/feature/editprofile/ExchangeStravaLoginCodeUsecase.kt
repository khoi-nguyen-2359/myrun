package akio.apps.myrun.feature.editprofile

import akio.apps.myrun.data.externalapp.model.ExternalAppToken

interface ExchangeStravaLoginCodeUsecase {
    suspend fun exchangeStravaLoginCode(code: String): ExternalAppToken.StravaToken
}