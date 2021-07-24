package akio.apps.myrun.feature.userprofile.impl

import akio.apps.myrun.domain.authentication.LogoutUsecase
import akio.apps.myrun.feature.routetracking.impl.ActivityUploadWorker
import akio.apps.myrun.feature.strava.impl.UploadStravaFileWorker
import android.content.Context
import javax.inject.Inject

class UserLogoutDelegate @Inject constructor(
    private val userLogoutUsecase: LogoutUsecase
) {
    suspend operator fun invoke(context: Context) {
        ActivityUploadWorker.clear(context)
        UploadStravaFileWorker.clear(context)
        userLogoutUsecase.logout()
    }
}
