package akio.apps.myrun.feature.userprofile.usecase

import akio.apps.myrun.data.externalapp.StravaApi
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.feature.userprofile.DeauthorizeStravaUsecase
import javax.inject.Inject

class DeauthorizeStravaUsecaseImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaTokenStorage: StravaTokenStorage
): DeauthorizeStravaUsecase {
    override suspend fun deauthorizeStrava() {
        val stravaToken = stravaTokenStorage.getToken()
            ?: return

        val authToken = "Bearer ${stravaToken.accessToken}"
        stravaApi.deauthorize(authToken, stravaToken.accessToken)
    }
}