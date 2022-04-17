package akio.apps.myrun.feature.profile.di

import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.profile.UploadAvatarActivity
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        AuthenticationDataModule::class,
        UserDataModule::class
    ],
)
internal interface UploadAvatarFeatureComponent {
    fun inject(activity: UploadAvatarActivity)

    @Component.Factory
    interface Factory {
        fun create(): UploadAvatarFeatureComponent
    }
}
