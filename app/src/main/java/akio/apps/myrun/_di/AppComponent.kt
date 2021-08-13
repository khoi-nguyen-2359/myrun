package akio.apps.myrun._di

import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.tracking.wiring.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.wiring.TrackingDataComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        // modules to inject into MyRunApp
        DispatchersModule::class
    ],
    dependencies = [
        AuthenticationDataComponent::class,
        TrackingDataComponent::class
    ]
)
interface AppComponent {
    // Application injection method
    fun inject(myRunApp: MyRunApp)

    @Component.Factory
    interface Factory {
        fun create(
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create(),
        ): AppComponent
    }

    interface Holder {
        fun getAppComponent(): AppComponent
    }
}
