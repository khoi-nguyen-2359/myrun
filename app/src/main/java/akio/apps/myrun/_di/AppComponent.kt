package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.wiring.data.tracking.DaggerTrackingDataComponent
import akio.apps.myrun.wiring.data.tracking.TrackingDataComponent
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [
        TrackingDataComponent::class,
        DomainComponent::class
    ]
)
interface AppComponent {
    // Application injection method
    fun inject(myRunApp: MyRunApp)

    @Component.Factory
    interface Factory {
        fun create(
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create(),
            domainComponent: DomainComponent = DaggerDomainComponent.create()
        ): AppComponent
    }

    interface Holder {
        fun getAppComponent(): AppComponent
    }
}
