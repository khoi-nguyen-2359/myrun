package akio.apps.myrun.di

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.base.di.AppScope
import akio.apps.myrun.data.tracking.di.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.di.TrackingDataComponent
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(
    dependencies = [TrackingDataComponent::class]
)
interface AppComponent {
    // Application injection method
    fun inject(myRunApp: MyRunApp)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            trackingDataComponent: TrackingDataComponent =
                DaggerTrackingDataComponent.factory().create(application),
        ): AppComponent
    }

    interface Holder {
        fun getAppComponent(): AppComponent
    }
}
