package akio.apps.myrun.feature.strava

import akio.apps.myrun.data.externalapp.model.StravaRoute

interface GetStravaRoutesUsecase {
    suspend fun getStravaRoutes(): List<StravaRoute>
}
