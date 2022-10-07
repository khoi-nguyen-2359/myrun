package akio.apps.myrun.feature.main.di

import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.tracking.di.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.di.TrackingDataComponent
import akio.apps.myrun.feature.main.HomeTabViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(dependencies = [TrackingDataComponent::class])
interface HomeTabFeatureComponent {
    fun homeTabViewModel(): HomeTabViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            trackingDataComponent: TrackingDataComponent =
                DaggerTrackingDataComponent.factory().create(application),
        ): HomeTabFeatureComponent
    }
}
