package akio.apps.myrun.data.eapps.api

import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.StravaRouteModel
import java.io.File

interface StravaDataRepository {
    suspend fun saveActivity(
        stravaToken: ExternalAppToken.StravaToken,
        activityTitle: String,
        activityFile: File,
    )

    suspend fun getRoutes(stravaToken: ExternalAppToken.StravaToken): List<StravaRouteModel>
}
