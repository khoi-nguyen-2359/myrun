package akio.apps.myrun.worker

import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.domain.migration.wiring.AppMigrationDomainComponent
import akio.apps.myrun.domain.migration.wiring.DaggerAppMigrationDomainComponent
import akio.apps.myrun.domain.strava.wiring.DaggerStravaDomainComponent
import akio.apps.myrun.domain.strava.wiring.StravaDomainComponent
import akio.apps.myrun.domain.tracking.wiring.DaggerTrackingDomainComponent
import akio.apps.myrun.domain.tracking.wiring.TrackingDomainComponent
import akio.apps.myrun.domain.user.wiring.DaggerUserDomainComponent
import akio.apps.myrun.domain.user.wiring.UserDomainComponent
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        StravaDomainComponent::class,
        UserDomainComponent::class,
        AppMigrationDomainComponent::class,
        TrackingDomainComponent::class
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
            trackingDomainComponent: TrackingDomainComponent = DaggerTrackingDomainComponent.factory().create(),
            stravaDomainComponent: StravaDomainComponent =
                DaggerStravaDomainComponent.factory().create(),
            userDomainComponent: UserDomainComponent = DaggerUserDomainComponent.factory().create(),
            appMigrationDomainComponent: AppMigrationDomainComponent =
                DaggerAppMigrationDomainComponent.factory().create()
        ): WorkerFeatureComponent
    }
}
