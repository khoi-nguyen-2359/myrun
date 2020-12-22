package akio.apps.myrun.feature.editprofile.impl

import akio.apps.myrun.data.externalapp.StravaTokenRepository
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.feature.editprofile.ExchangeStravaLoginCodeUsecase
import javax.inject.Inject

class ExchangeStravaLoginCodeUsecaseImpl @Inject constructor(
    private val stravaTokenRepository: StravaTokenRepository
): ExchangeStravaLoginCodeUsecase {
    override suspend fun exchangeStravaLoginCode(code: String): ExternalAppToken.StravaToken {
        return stravaTokenRepository.exchangeToken(code)
    }
}