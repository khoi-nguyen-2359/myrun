package akio.apps.myrun.feature.profile

import akio.apps.myrun.domain.user.impl.LogoutUsecase
import android.content.Context
import timber.log.Timber

class UserLogoutDelegate(private val userLogoutUsecase: LogoutUsecase) {
    suspend operator fun invoke(context: Context) {
        Timber.d(context.toString())
        // TODO: broadcast intent for these actions
//        ActivityUploadWorker.clear(context)
//        UploadStravaFileWorker.clear(context)
        userLogoutUsecase.logout()
    }
}
