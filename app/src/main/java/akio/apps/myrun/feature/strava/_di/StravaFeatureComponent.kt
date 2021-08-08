package akio.apps.myrun.feature.strava._di

import akio.apps.base.feature.viewmodel.ViewModelFactoryProvider
import akio.apps.base.wiring.DispatchersModule
import akio.apps.base.wiring.FeatureScope
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.external.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.external.wiring.ExternalAppDataComponent
import akio.apps.myrun.feature.strava.impl.UploadStravaFileWorker
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        StravaFeatureModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        AppComponent::class,
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
            @BindsInstance application: Application,
            appComponent: AppComponent = (application as AppComponent.Holder).getAppComponent(),
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.create()
        ): StravaFeatureComponent
    }
}
