package akio.apps.myrun.feature.registration

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.authentication.di.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.di.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.eapps.di.DaggerExternalAppDataComponent
import akio.apps.myrun.data.eapps.di.ExternalAppDataComponent
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingModule
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
internal interface SignInFeatureComponent {
    fun signInViewModel(): SignInViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.factory().create(application),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(application),
        ): SignInFeatureComponent
    }
}
