package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.data.externalapp.StravaDataRepository
import akio.apps.myrun.data.externalapp.impl.StravaApi
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.model.StravaRoute
import akio.apps.myrun.feature.strava.GetStravaRoutesUsecase
import javax.inject.Inject

class GetStravaRoutesUsecaseImpl @Inject constructor(
    private val stravaTokenStorage: StravaTokenStorage,
    private val stravaDataRepository: StravaDataRepository
): GetStravaRoutesUsecase {
    override suspend fun getStravaRoutes(): List<StravaRoute> {
        val athleteId = stravaTokenStorage.getToken()?.athlete?.id
            ?: return emptyList()

        return stravaDataRepository.getRoutes(athleteId)
    }
}