package akio.apps.myrun.feature.userhome._di

import akio.apps.common.wiring.FeatureScope
import akio.apps.myrun.feature.userhome.UserHomeViewModel
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    dependencies = [
        DomainComponent::class
    ]
)
interface UserHomeFeatureComponent {
    fun userHomeViewModel(): UserHomeViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance savedStateHandle: SavedStateHandle,
            domainComponent: DomainComponent = DaggerDomainComponent.create(),
        ): UserHomeFeatureComponent
    }
}