package akio.apps.myrun.feature.activitydetail.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.data.activity.ActivityDataModule
import akio.apps.myrun.feature.activitydetail.ActivityExportService
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
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
