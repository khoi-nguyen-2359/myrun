package akio.apps.myrun.wiring

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.domain.tracking.wiring.DaggerTrackingDomainComponent
import akio.apps.myrun.domain.tracking.wiring.TrackingDomainComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [
        TrackingDomainComponent::class,
    ]
)
interface AppComponent {
    // Application injection method
    fun inject(myRunApp: MyRunApp)

    @Component.Factory
    interface Factory {
        fun create(
            trackingDataComponent: TrackingDomainComponent =
                DaggerTrackingDomainComponent.factory().create(),
        ): AppComponent
    }

    interface Holder {
        fun getAppComponent(): AppComponent
    }
}
