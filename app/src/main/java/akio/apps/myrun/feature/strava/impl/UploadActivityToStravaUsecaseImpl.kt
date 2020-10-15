package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.data.externalapp.StravaDataRepository
import akio.apps.myrun.feature.strava.UploadActivityToStravaUsecase
import java.io.File
import javax.inject.Inject

class UploadActivityToStravaUsecaseImpl @Inject constructor(
    private val stravaDataRepository: StravaDataRepository
) : UploadActivityToStravaUsecase {
    override suspend fun uploadActivityToStrava(activityTitle: String, activityFile: File) {
        stravaDataRepository.saveActivity(activityTitle, activityFile)
    }
}