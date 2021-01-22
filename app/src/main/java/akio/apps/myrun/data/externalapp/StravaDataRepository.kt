package akio.apps.myrun.data.externalapp

import akio.apps.myrun.data.externalapp.model.StravaRoute
import java.io.File

interface StravaDataRepository {
    suspend fun saveActivity(activityTitle: String, activityFile: File)
    suspend fun getRoutes(athleteId: Long): List<StravaRoute>
}
