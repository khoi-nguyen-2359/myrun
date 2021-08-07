package akio.apps.myrun.data.activity.wiring

import akio.apps.base.wiring.ApplicationModule
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data._base.wiring.FirebaseDataModule
import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.ActivityTcxFileWriter
import dagger.Component

@Component(modules = [
    ActivityDataModule::class,
    FirebaseDataModule::class,
    DispatchersModule::class,
    ApplicationModule::class
])
interface ActivityDataComponent {
    fun activityLocalStorage(): ActivityLocalStorage
    fun activityRepository(): ActivityRepository
    fun tcxFileWriter(): ActivityTcxFileWriter
}
