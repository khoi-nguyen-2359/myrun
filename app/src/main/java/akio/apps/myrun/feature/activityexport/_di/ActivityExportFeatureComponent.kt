package akio.apps.myrun.feature.activityexport._di

import akio.apps.base.wiring.ApplicationModule
import akio.apps.base.wiring.DispatchersModule
import akio.apps.base.wiring.FeatureScope
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.feature.activityexport.ActivityExportService
import dagger.Component

@FeatureScope
@Component(
    modules = [DispatchersModule::class, ApplicationModule::class],
    dependencies = [ActivityDataComponent::class]
)
interface ActivityExportFeatureComponent {
    fun inject(activityExportService: ActivityExportService)

    @Component.Factory
    interface Factory {
        fun create(
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create()
        ): ActivityExportFeatureComponent
    }
}
