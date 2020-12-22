package akio.apps.myrun.feature.strava

interface UploadActivityFilesToStravaUsecase {
    suspend fun upload(): Int
}