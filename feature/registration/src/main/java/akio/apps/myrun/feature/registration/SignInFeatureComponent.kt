package akio.apps.myrun.feature.registration

import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.domain.user.wiring.DaggerUserDomainComponent
import akio.apps.myrun.domain.user.wiring.UserDomainComponent
import dagger.Component

@FeatureScope
@Component(
    modules = [LaunchCatchingModule::class],
    dependencies = [
        AuthenticationDataComponent::class,
        UserDomainComponent::class
    ]
)
interface SignInFeatureComponent {
    fun signInViewModel(): SignInViewModel

    @Component.Factory
    interface Factory {
        fun create(
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDomainComponent: UserDomainComponent = DaggerUserDomainComponent.factory().create()
        ): SignInFeatureComponent
    }
}
