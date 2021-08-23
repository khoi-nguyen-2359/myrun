package akio.apps.myrun.feature.activityexport._di

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.common.wiring.FeatureScope
import akio.apps.myrun.feature.activityexport.ActivityExportService
import akio.apps.myrun.wiring.data.activity.ActivityDataComponent
import akio.apps.myrun.wiring.data.activity.DaggerActivityDataComponent
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
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
        ): ActivityExportFeatureComponent
    }
}
