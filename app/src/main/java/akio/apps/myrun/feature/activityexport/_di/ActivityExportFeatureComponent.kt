package akio.apps.myrun.feature.activityexport._di

import akio.apps.base.wiring.DispatchersModule
import akio.apps.base.wiring.FeatureScope
import akio.apps.myrun._di.AppComponent
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.feature.activityexport.ActivityExportService
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [DispatchersModule::class],
    dependencies = [AppComponent::class, ActivityDataComponent::class]
)
interface ActivityExportFeatureComponent {
    fun inject(activityExportService: ActivityExportService)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            appComponent: AppComponent = (application as AppComponent.Holder).getAppComponent(),
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create()
        ): ActivityExportFeatureComponent
    }
}
