package akio.apps.myrun.feature.strava

import akio.apps.myrun.data.externalapp.entity.StravaRoute

interface GetStravaRoutesUsecase {
    suspend fun getStravaRoutes(): List<StravaRoute>
}