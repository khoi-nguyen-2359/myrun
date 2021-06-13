package akio.apps.myrun.feature.activityexport._di

import akio.apps._base.di.AppDependantComponentFactory
import akio.apps._base.di.FeatureScope
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun._di.DispatchersModule
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.feature.activityexport.ActivityExportService
import dagger.Component

@FeatureScope
@Component(
    modules = [
        ActivityDataModule::class,
        DispatchersModule::class
    ],
    dependencies = [AppComponent::class]
)
interface ActivityExportFeatureComponent {
    fun inject(activityExportService: ActivityExportService)

    @Component.Factory
    interface Factory : AppDependantComponentFactory<ActivityExportFeatureComponent>
}
