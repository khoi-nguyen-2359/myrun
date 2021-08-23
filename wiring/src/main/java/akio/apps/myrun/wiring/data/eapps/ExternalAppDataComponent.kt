package akio.apps.myrun.wiring.data.eapps

import akio.apps.common.wiring.ApplicationModule
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaDataRepository
import akio.apps.myrun.data.eapps.api.StravaTokenRepository
import akio.apps.myrun.data.wiring.FirebaseDataModule
import akio.apps.myrun.data.wiring.NetworkModule
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataComponent
import akio.apps.myrun.wiring.data.authentication.DaggerAuthenticationDataComponent
import dagger.Component

@Component(
    modules = [
        ExternalAppDataModule::class,
        FirebaseDataModule::class,
        NetworkModule::class,
        ApplicationModule::class
    ],
    dependencies = [AuthenticationDataComponent::class]
)
interface ExternalAppDataComponent {
    fun stravaTokenRepository(): StravaTokenRepository
    fun externalAppRepository(): ExternalAppProvidersRepository
    fun stravaDataRepository(): StravaDataRepository

    @Component.Factory
    interface Factory {
        fun create(
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
        ): ExternalAppDataComponent
    }
}
