package akio.apps.myrun.feature.activitydetail.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.activity.di.ActivityDataComponent
import akio.apps.myrun.data.activity.di.DaggerActivityDataComponent
import akio.apps.myrun.feature.activitydetail.ActivityExportService
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        DispatchersModule::class,
    ],
    dependencies = [ActivityDataComponent::class]
)
internal interface ActivityExportFeatureComponent {
    fun inject(activityExportService: ActivityExportService)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.factory().create(application),
        ): ActivityExportFeatureComponent
    }
}
