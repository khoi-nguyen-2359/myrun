package akio.apps.myrun.domain.userprofile

import akio.apps.myrun.data.externalapp.StravaTokenRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import javax.inject.Inject

class DeauthorizeStravaUsecase @Inject constructor(
    private val stravaTokenRepository: StravaTokenRepository,
    private val stravaTokenStorage: StravaTokenStorage
) {
    suspend fun deauthorizeStrava() {
        val stravaToken = stravaTokenStorage.getToken()
            ?: return

        stravaTokenRepository.deauthorizeToken(stravaToken.accessToken)
    }
}
