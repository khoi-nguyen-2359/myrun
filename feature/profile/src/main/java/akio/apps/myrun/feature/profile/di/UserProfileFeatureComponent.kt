package akio.apps.myrun.feature.profile.di

import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.eapps.ExternalAppDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.domain.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.profile.UserProfileViewModel
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        AuthenticationDataModule::class,
        ExternalAppDataModule::class,
        UserDataModule::class
    ],
)
internal interface UserProfileFeatureComponent {
    fun userProfileViewModel(): UserProfileViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance savedStateHandle: SavedStateHandle,
        ): UserProfileFeatureComponent
    }
}
