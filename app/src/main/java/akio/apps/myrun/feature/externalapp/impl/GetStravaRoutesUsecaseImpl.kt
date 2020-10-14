package akio.apps.myrun.feature.externalapp.impl

import akio.apps.myrun.data.externalapp.StravaApi
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.entity.StravaRoute
import akio.apps.myrun.feature.externalapp.GetStravaRoutesUsecase
import javax.inject.Inject

class GetStravaRoutesUsecaseImpl @Inject constructor(
    val stravaTokenStorage: StravaTokenStorage,
    val stravaApi: StravaApi
): GetStravaRoutesUsecase {
    override suspend fun getStravaRoutes(): List<StravaRoute> {
        val stravaToken = stravaTokenStorage.getToken()
            ?: return emptyList()

        return stravaApi.getAthleteRoutes(
            "Bearer ${stravaToken.accessToken}",
            stravaToken.athlete.id
        )
    }
}