package akio.apps.myrun.feature.strava

import java.io.File

interface UploadActivityToStravaUsecase {
    suspend fun uploadActivityToStrava(activityTitle: String, activityFile: File)
}