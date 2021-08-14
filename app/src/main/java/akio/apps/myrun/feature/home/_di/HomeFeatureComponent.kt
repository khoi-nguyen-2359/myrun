package akio.apps.myrun.feature.home._di

import akio.apps.common.feature.viewmodel.ViewModelFactoryProvider
import akio.apps.common.wiring.DispatchersModule
import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.tracking.wiring.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.wiring.TrackingDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import dagger.Component

@Component(
    modules = [
        HomeFeatureModule::class,
        DispatchersModule::class,
        LaunchCatchingModule::class
    ],
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        UserDataComponent::class,
        TrackingDataComponent::class
    ]
)
interface HomeFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            trackingDataComponent: TrackingDataComponent = DaggerTrackingDataComponent.create()
        ): HomeFeatureComponent
    }
}
