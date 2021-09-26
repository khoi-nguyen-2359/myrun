package akio.apps.myrun.feature.profile

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.FeatureScope
import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        ApplicationModule::class
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
