package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.eapps.api.StravaTokenRepository
import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import javax.inject.Inject

class ExchangeStravaLoginCodeUsecase @Inject constructor(
    private val stravaTokenRepository: StravaTokenRepository
) {
    suspend fun exchangeStravaLoginCode(code: String): ExternalAppToken.StravaToken {
        return stravaTokenRepository.exchangeToken(code)
    }
}
