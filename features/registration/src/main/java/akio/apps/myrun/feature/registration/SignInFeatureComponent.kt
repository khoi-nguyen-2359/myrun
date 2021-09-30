package akio.apps.myrun.feature.registration

import akio.apps.common.wiring.FeatureScope
import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.Component

@FeatureScope
@Component(
    modules = [LaunchCatchingModule::class],
    dependencies = [
        AuthenticationDataComponent::class,
        DomainComponent::class
    ]
)
interface SignInFeatureComponent {
    fun signInViewModel(): SignInViewModel

    @Component.Factory
    interface Factory {
        fun create(
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            domainComponent: DomainComponent = DaggerDomainComponent.factory().create(),
        ): SignInFeatureComponent
    }
}
