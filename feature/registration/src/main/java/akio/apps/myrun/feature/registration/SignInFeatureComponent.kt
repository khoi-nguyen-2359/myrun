package akio.apps.myrun.feature.registration

import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.eapps.ExternalAppDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.domain.launchcatching.LaunchCatchingModule
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        AuthenticationDataModule::class,
        ExternalAppDataModule::class,
        UserDataModule::class,
    ],
)
internal interface SignInFeatureComponent {
    fun signInViewModel(): SignInViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
        ): SignInFeatureComponent
    }
}
