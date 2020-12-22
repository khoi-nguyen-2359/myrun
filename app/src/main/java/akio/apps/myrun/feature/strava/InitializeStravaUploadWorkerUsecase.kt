package akio.apps.myrun.feature.strava

interface InitializeStravaUploadWorkerUsecase {
    suspend fun mayInitializeWorker()
}