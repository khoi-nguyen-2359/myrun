package akio.apps.myrun.feature.strava

import java.io.File

interface UploadRunToStravaUsecase {
    suspend fun uploadRunToStrava(runTitle: String, runFile: File)
}