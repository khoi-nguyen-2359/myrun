package akio.apps.myrun.domain.editprofile

import akio.apps.myrun.data.externalapp.StravaTokenRepository
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import javax.inject.Inject

class ExchangeStravaLoginCodeUsecase @Inject constructor(
    private val stravaTokenRepository: StravaTokenRepository
) {
    suspend fun exchangeStravaLoginCode(code: String): ExternalAppToken.StravaToken {
        return stravaTokenRepository.exchangeToken(code)
    }
}
