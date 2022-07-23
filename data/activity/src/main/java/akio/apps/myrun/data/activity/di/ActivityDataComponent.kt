package akio.apps.myrun.data.activity.di

import akio.apps.myrun.base.firebase.FirebaseDataModule
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.ActivityTcxFileWriter
import android.app.Application
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

abstract class ActivityDataScope private constructor()

@Singleton
@MergeComponent(
    scope = ActivityDataScope::class,
    modules = [FirebaseDataModule::class]
)
interface ActivityDataComponent {
    fun activityLocalStorage(): ActivityLocalStorage
    fun activityRepository(): ActivityRepository
    fun activityTcxFileWriter(): ActivityTcxFileWriter

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ActivityDataComponent
    }
}
