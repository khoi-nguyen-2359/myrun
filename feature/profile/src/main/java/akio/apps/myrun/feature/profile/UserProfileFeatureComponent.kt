package akio.apps.myrun.feature.profile

import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@akio.apps.myrun.data.wiring.FeatureScope
@Component(
    modules = [
        akio.apps.myrun.data.wiring.LaunchCatchingModule::class,
        akio.apps.myrun.data.wiring.ApplicationModule::class
    ],
    dependencies = [
        DomainComponent::class
    ]
)
interface UserProfileFeatureComponent {
    fun userProfileViewModel(): UserProfileViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance savedStateHandle: SavedStateHandle,
            domainComponent: DomainComponent = DaggerDomainComponent.factory().create(),
        ): UserProfileFeatureComponent
    }
}
