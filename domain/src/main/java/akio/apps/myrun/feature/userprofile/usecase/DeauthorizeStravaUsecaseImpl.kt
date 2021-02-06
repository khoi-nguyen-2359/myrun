package akio.apps.myrun.feature.userprofile.usecase

import akio.apps.myrun.data.externalapp.StravaTokenRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.feature.userprofile.DeauthorizeStravaUsecase
import javax.inject.Inject

class DeauthorizeStravaUsecaseImpl @Inject constructor(
    private val stravaTokenRepository: StravaTokenRepository,
    private val stravaTokenStorage: StravaTokenStorage
) : DeauthorizeStravaUsecase {
    override suspend fun deauthorizeStrava() {
        val stravaToken = stravaTokenStorage.getToken()
            ?: return

        stravaTokenRepository.deauthorizeToken(stravaToken.accessToken)
    }
}
