package akio.apps.myrun.feature.profile.wiring

import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.domain.user.wiring.DaggerUserDomainComponent
import akio.apps.myrun.domain.user.wiring.UserDomainComponent
import akio.apps.myrun.feature.profile.UploadAvatarActivity
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class
    ],
    dependencies = [
        UserDataComponent::class,
        AuthenticationDataComponent::class,
        UserDomainComponent::class
    ]
)
interface UploadAvatarFeatureComponent {
    fun inject(activity: UploadAvatarActivity)

    @Component.Factory
    interface Factory {
        fun create(
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDomainComponent: UserDomainComponent = DaggerUserDomainComponent.factory().create()
        ): UploadAvatarFeatureComponent
    }
}
