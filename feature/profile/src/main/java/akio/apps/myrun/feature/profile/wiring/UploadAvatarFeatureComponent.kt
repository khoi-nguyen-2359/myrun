package akio.apps.myrun.feature.profile.wiring

import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.domain.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.profile.UploadAvatarActivity
import akio.apps.myrun.wiring.common.FeatureScope
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        AuthenticationDataModule::class,
        UserDataModule::class
    ],
)
interface UploadAvatarFeatureComponent {
    fun inject(activity: UploadAvatarActivity)

    @Component.Factory
    interface Factory {
        fun create(): UploadAvatarFeatureComponent
    }
}
