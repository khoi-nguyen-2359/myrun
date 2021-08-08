package akio.apps.myrun.feature.registration.impl

import akio.apps.base.wiring.FeatureScope
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.external.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.external.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.userprofile.wiring.DaggerUserProfileDataComponent
import akio.apps.myrun.data.userprofile.wiring.UserProfileDataComponent
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    dependencies = [
        UserProfileDataComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
interface SignInFeatureComponent {
    fun signInViewModel(): SignInViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userProfileDataComponent: UserProfileDataComponent =
                DaggerUserProfileDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.create()
        ): SignInFeatureComponent
    }
}
