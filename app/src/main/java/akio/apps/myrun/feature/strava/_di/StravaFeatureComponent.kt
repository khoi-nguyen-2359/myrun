package akio.apps.myrun.feature.strava._di

import akio.apps.common.feature.viewmodel.ViewModelFactoryProvider
import akio.apps.common.wiring.DispatchersModule
import akio.apps.common.wiring.FeatureScope
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.eapps.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.eapps.wiring.ExternalAppDataComponent
import akio.apps.myrun.feature.strava.impl.UploadStravaFileWorker
import dagger.Component

@FeatureScope
@Component(
    modules = [
        StravaFeatureModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
interface StravaFeatureComponent : ViewModelFactoryProvider {
    fun inject(uploadStravaFileWorker: UploadStravaFileWorker)

    @Component.Factory
    interface Factory {
        fun create(
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(),
        ): StravaFeatureComponent
    }
}
