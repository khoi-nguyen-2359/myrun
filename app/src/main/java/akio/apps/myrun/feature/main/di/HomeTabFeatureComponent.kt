package akio.apps.myrun.feature.main.di

import akio.apps.myrun.data.tracking.TrackingDataModule
import akio.apps.myrun.feature.main.HomeTabViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [TrackingDataModule::class])
interface HomeTabFeatureComponent {
    fun homeTabViewModel(): HomeTabViewModel

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): HomeTabFeatureComponent
    }
}
