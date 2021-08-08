package akio.apps.myrun.data.externalapp

import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.StravaRouteModel
import java.io.File

interface StravaDataRepository {
    suspend fun saveActivity(
        stravaToken: ExternalAppToken.StravaToken,
        activityTitle: String,
        activityFile: File
    )

    suspend fun getRoutes(stravaToken: ExternalAppToken.StravaToken): List<StravaRouteModel>
}
