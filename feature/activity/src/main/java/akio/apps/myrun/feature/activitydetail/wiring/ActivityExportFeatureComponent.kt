package akio.apps.myrun.feature.activitydetail.wiring

import akio.apps.myrun.data.activity.ActivityDataModule
import akio.apps.myrun.data.common.di.DispatchersModule
import akio.apps.myrun.feature.activitydetail.ActivityExportService
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        DispatchersModule::class,
        ActivityDataModule::class
    ],
)
internal interface ActivityExportFeatureComponent {
    fun inject(activityExportService: ActivityExportService)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
        ): ActivityExportFeatureComponent
    }
}
