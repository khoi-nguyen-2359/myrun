package akio.apps.myrun.worker

import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.activity.ActivityDataModule
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.eapps.ExternalAppDataModule
import akio.apps.myrun.data.location.LocationDataModule
import akio.apps.myrun.data.tracking.TrackingDataModule
import akio.apps.myrun.data.user.UserDataModule
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        AuthenticationDataModule::class,
        ExternalAppDataModule::class,
        LocationDataModule::class,
        UserDataModule::class,
        ActivityDataModule::class,
        TrackingDataModule::class
    ],
)
interface WorkerFeatureComponent {
    fun inject(uploadStravaFileWorker: UploadStravaFileWorker)
    fun inject(updateUserRecentPlaceWorker: UpdateUserRecentPlaceWorker)
    fun inject(activityUploadWorker: ActivityUploadWorker)
    fun inject(appMigrationWorker: AppMigrationWorker)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
        ): WorkerFeatureComponent
    }
}
