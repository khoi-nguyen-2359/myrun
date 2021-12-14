package akio.apps.myrun.domain.migration.wiring

import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.domain.migration.impl.AppMigrationUsecase
import akio.apps.myrun.domain.tracking.wiring.DaggerTrackingDomainComponent
import akio.apps.myrun.domain.tracking.wiring.TrackingDomainComponent
import dagger.Component

@Component(
    dependencies = [
        UserDataComponent::class,
        TrackingDomainComponent::class
    ]
)
interface AppMigrationDomainComponent {
    fun AppMigrationUsecase(): AppMigrationUsecase

    @Component.Factory
    interface Factory {
        fun create(
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            trackingDataComponent: TrackingDomainComponent = DaggerTrackingDomainComponent.factory()
                .create(),
        ): AppMigrationDomainComponent
    }
}
