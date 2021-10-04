package akio.apps.myrun.wiring

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data.tracking.wiring.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.wiring.TrackingDataComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [
        TrackingDataComponent::class,
    ]
)
interface AppComponent {
    // Application injection method
    fun inject(myRunApp: MyRunApp)

    @Component.Factory
    interface Factory {
        fun create(
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create(),
        ): AppComponent
    }

    interface Holder {
        fun getAppComponent(): AppComponent
    }
}
