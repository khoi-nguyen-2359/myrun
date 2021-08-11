package akio.apps.myrun.data.activity.wiring

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.ActivityTcxFileWriter
import akio.apps.myrun.data.wiring.FirebaseDataModule
import dagger.Component

@Component(
    modules = [
        ActivityDataModule::class,
        FirebaseDataModule::class,
        DispatchersModule::class,
        ApplicationModule::class
    ]
)
interface ActivityDataComponent {
    fun activityLocalStorage(): ActivityLocalStorage
    fun activityRepository(): ActivityRepository
    fun tcxFileWriter(): ActivityTcxFileWriter
}
