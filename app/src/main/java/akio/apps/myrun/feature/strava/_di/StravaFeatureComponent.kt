package akio.apps.myrun.feature.strava._di

import akio.apps._base.di.AppDependantComponentFactory
import akio.apps._base.di.FeatureScope
import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun._di.DispatchersModule
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.data.activityexport._di.ActivityFileDataModule
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import akio.apps.myrun.feature.activityexport.ActivityExportService
import akio.apps.myrun.feature.strava.impl.UploadStravaFileWorker
import dagger.Component

@FeatureScope
@Component(
    modules = [
        StravaFeatureModule::class,
        ExternalAppDataModule::class,
        AuthenticationDataModule::class,
        ActivityFileDataModule::class,
        ActivityDataModule::class,
        DispatchersModule::class
    ],
    dependencies = [AppComponent::class]
)
interface StravaFeatureComponent : ViewModelFactoryProvider {
    fun inject(uploadStravaFileWorker: UploadStravaFileWorker)
    fun inject(activityExportService: ActivityExportService)

    @Component.Factory
    interface Factory : AppDependantComponentFactory<StravaFeatureComponent>
}
