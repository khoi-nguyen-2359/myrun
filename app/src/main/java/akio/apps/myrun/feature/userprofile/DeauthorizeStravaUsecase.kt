package akio.apps.myrun.feature.userprofile

interface DeauthorizeStravaUsecase {
    suspend fun deauthorizeStrava()
}