package akio.apps.myrun.di

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.data.tracking.TrackingDataModule
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        TrackingDataModule::class
    ]
)
interface AppComponent {
    // Application injection method
    fun inject(myRunApp: MyRunApp)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): AppComponent
    }

    interface Holder {
        fun getAppComponent(): AppComponent
    }
}