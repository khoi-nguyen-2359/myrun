package akio.apps.myrun.feature.profile.wiring

import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.domain.strava.wiring.DaggerStravaDomainComponent
import akio.apps.myrun.domain.strava.wiring.StravaDomainComponent
import akio.apps.myrun.feature.profile.LinkStravaViewModel
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class
    ],
    dependencies = [
        StravaDomainComponent::class
    ]
)
interface LinkStravaComponent {
    fun linkStravaViewModel(): LinkStravaViewModel

    @Component.Factory
    interface Factory {
        fun create(
            stravaDomainComponent: StravaDomainComponent =
                DaggerStravaDomainComponent.factory().create()
        ): LinkStravaComponent
    }
}
