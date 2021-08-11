package akio.apps.myrun.data.activity.wiring

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data.wiring.FirebaseDataModule
import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.ActivityTcxFileWriter
import dagger.Component

@Component(
    modules = [
        ActivityDataModule::class,
        akio.apps.myrun.data.wiring.FirebaseDataModule::class,
        DispatchersModule::class,
        ApplicationModule::class
    ]
)
interface ActivityDataComponent {
    fun activityLocalStorage(): ActivityLocalStorage
    fun activityRepository(): ActivityRepository
    fun tcxFileWriter(): ActivityTcxFileWriter
}
