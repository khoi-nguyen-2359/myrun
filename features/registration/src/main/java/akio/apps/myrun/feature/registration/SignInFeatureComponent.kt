package akio.apps.myrun.feature.registration

import akio.apps.common.wiring.FeatureScope
import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataComponent
import akio.apps.myrun.wiring.data.authentication.DaggerAuthenticationDataComponent
import akio.apps.myrun.wiring.data.eapps.DaggerExternalAppDataComponent
import akio.apps.myrun.wiring.data.eapps.ExternalAppDataComponent
import akio.apps.myrun.wiring.data.user.DaggerUserDataComponent
import akio.apps.myrun.wiring.data.user.UserDataComponent
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.Component

@FeatureScope
@Component(
    modules = [LaunchCatchingModule::class],
    dependencies = [
        UserDataComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class,
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
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(),
            domainComponent: DomainComponent = DaggerDomainComponent.create()
        ): SignInFeatureComponent
    }
}
