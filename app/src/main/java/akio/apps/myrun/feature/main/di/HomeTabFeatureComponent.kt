package akio.apps.myrun.feature.main.di

import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.di.DaggerTrackingDataComponent
import akio.apps.myrun.data.tracking.di.TrackingDataComponent
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(dependencies = [TrackingDataComponent::class])
interface HomeTabFeatureComponent {
    fun routeTrackingState(): RouteTrackingState

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            trackingDataComponent: TrackingDataComponent =
                DaggerTrackingDataComponent.factory().create(application),
        ): HomeTabFeatureComponent
    }
}
