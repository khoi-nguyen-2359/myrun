package akio.apps.myrun.feature.profile

import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.Component

@akio.apps.myrun.data.wiring.FeatureScope
@Component(
    modules = [
        akio.apps.myrun.data.wiring.LaunchCatchingModule::class
    ],
    dependencies = [
        DomainComponent::class
    ]
)
interface LinkStravaComponent {
    fun linkStravaViewModel(): LinkStravaViewModel

    @Component.Factory
    interface Factory {
        fun create(
            domainComponent: DomainComponent = DaggerDomainComponent.factory().create(),
        ): LinkStravaComponent
    }
}
