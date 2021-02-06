package akio.apps.myrun.feature.strava

interface InitializeStravaUploadWorkerDelegate {
    suspend fun mayInitializeWorker()
}
