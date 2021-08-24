package akio.apps.myrun.worker

import akio.apps.common.wiring.DispatchersModule
import akio.apps.common.wiring.FeatureScope
import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        DomainComponent::class
    ]
)
interface WorkerFeatureComponent {
    fun inject(uploadStravaFileWorker: UploadStravaFileWorker)
    fun inject(updateUserRecentPlaceWorker: UpdateUserRecentPlaceWorker)
    fun inject(activityUploadWorker: ActivityUploadWorker)

    @Component.Factory
    interface Factory {
        fun create(
            domainComponent: DomainComponent = DaggerDomainComponent.create(),
        ): WorkerFeatureComponent
    }
}
