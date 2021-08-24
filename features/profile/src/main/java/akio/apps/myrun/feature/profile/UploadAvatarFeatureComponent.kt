package akio.apps.myrun.feature.profile

import akio.apps.common.wiring.FeatureScope
import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.domain.user.UpdateUserProfileUsecase
import akio.apps.myrun.domain.user.UploadUserAvatarImageUsecase
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataComponent
import akio.apps.myrun.wiring.data.authentication.DaggerAuthenticationDataComponent
import akio.apps.myrun.wiring.data.user.DaggerUserDataComponent
import akio.apps.myrun.wiring.data.user.UserDataComponent
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
