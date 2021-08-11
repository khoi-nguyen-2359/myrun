package akio.apps.myrun.feature.registration

import akio.apps.common.wiring.FeatureScope
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.external.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.external.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import dagger.Component

@FeatureScope
@Component(
    dependencies = [
        akio.apps.myrun.data.user.wiring.UserDataComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
interface SignInFeatureComponent {
    fun signInViewModel(): SignInViewModel

    @Component.Factory
    interface Factory {
        fun create(
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDataComponent: akio.apps.myrun.data.user.wiring.UserDataComponent = DaggerUserDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.create()
        ): SignInFeatureComponent
    }
}
