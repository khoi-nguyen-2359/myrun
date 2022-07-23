package akio.apps.myrun.worker

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.activity.di.ActivityDataComponent
import akio.apps.myrun.data.activity.di.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.di.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.di.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.eapps.di.DaggerExternalAppDataComponent
import akio.apps.myrun.data.eapps.di.ExternalAppDataComponent
import akio.apps.myrun.data.location.di.DaggerLocationDataComponent
import akio.apps.myrun.data.location.di.LocationDataComponent
import akio.apps.myrun.data.tracking.di.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.di.TrackingDataComponent
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [DispatchersModule::class],
    dependencies = [
        LocationDataComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class,
        TrackingDataComponent::class,
        ActivityDataComponent::class
    ]
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
            locationDataComponent: LocationDataComponent =
                DaggerLocationDataComponent.factory().create(application),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.factory().create(application),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(application),
            trackingDataComponent: TrackingDataComponent =
                DaggerTrackingDataComponent.factory().create(application),
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.factory().create(application)
        ): WorkerFeatureComponent
    }
}
