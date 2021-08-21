package akio.apps.myrun.feature.profile

import akio.apps.common.wiring.FeatureScope
import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.domain.user.UpdateUserProfileUsecase
import akio.apps.myrun.domain.user.UploadUserAvatarImageUsecase
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class
    ],
    dependencies = [
        UserDataComponent::class,
        AuthenticationDataComponent::class,
    ]
)
interface UploadAvatarFeatureComponent {
    fun updateUserProfileUsecase(): UpdateUserProfileUsecase
    fun uploadUserAvatarImageUsecase(): UploadUserAvatarImageUsecase

    @Component.Factory
    interface Factory {
        fun create(
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
        ): UploadAvatarFeatureComponent
    }
}
