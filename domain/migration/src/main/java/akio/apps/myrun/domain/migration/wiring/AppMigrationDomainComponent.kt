package akio.apps.myrun.domain.migration.wiring

import akio.apps.myrun.data.tracking.wiring.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.wiring.TrackingDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.domain.migration.impl.AppMigrationUsecase
import dagger.Component

@Component(
    dependencies = [
        UserDataComponent::class,
        TrackingDataComponent::class
    ]
)
interface AppMigrationDomainComponent {
    fun AppMigrationUsecase(): AppMigrationUsecase

    @Component.Factory
    interface Factory {
        fun create(
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create()
        ): AppMigrationDomainComponent
    }
}