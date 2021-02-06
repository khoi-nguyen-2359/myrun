package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.externalapp.StravaDataRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.model.StravaRoute
import javax.inject.Inject

class GetStravaRoutesUsecase @Inject constructor(
    private val stravaTokenStorage: StravaTokenStorage,
    private val stravaDataRepository: StravaDataRepository
) {
    suspend fun getStravaRoutes(): List<StravaRoute> {
        val athleteId = stravaTokenStorage.getToken()?.athlete?.id
            ?: return emptyList()

        return stravaDataRepository.getRoutes(athleteId)
    }
}
