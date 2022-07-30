package akio.apps.myrun.feature.profile.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.authentication.di.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.di.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.eapps.di.DaggerExternalAppDataComponent
import akio.apps.myrun.data.eapps.di.ExternalAppDataComponent
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.profile.UserProfileViewModel
import akio.apps.myrun.feature.userprefs.UserPreferencesViewModel
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        DispatchersModule::class,
        LaunchCatchingModule::class
    ],
    dependencies = [
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
internal interface UserProfileFeatureComponent {
    fun userProfileViewModel(): UserProfileViewModel
    fun userPrefsViewModel(): UserPreferencesViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance savedStateHandle: SavedStateHandle,
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.factory().create(application),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(application),
        ): UserProfileFeatureComponent
    }
}
