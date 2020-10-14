package akio.apps.myrun.feature.externalapp

import java.io.File

interface UploadRunToStravaUsecase {
    suspend fun uploadRunToStrava(runTitle: String, runFile: File)
}