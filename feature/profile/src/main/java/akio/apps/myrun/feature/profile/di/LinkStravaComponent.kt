package akio.apps.myrun.feature.profile.di

import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.eapps.ExternalAppDataModule
import akio.apps.myrun.domain.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.profile.LinkStravaViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        AuthenticationDataModule::class,
        ExternalAppDataModule::class
    ]
)
internal interface LinkStravaComponent {
    fun linkStravaViewModel(): LinkStravaViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
        ): LinkStravaComponent
    }
}
