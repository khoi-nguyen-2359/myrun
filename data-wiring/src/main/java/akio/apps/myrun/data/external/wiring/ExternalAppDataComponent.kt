package akio.apps.myrun.data.external.wiring

import akio.apps.common.wiring.ApplicationModule
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaDataRepository
import akio.apps.myrun.data.externalapp.StravaTokenRepository
import akio.apps.myrun.data.wiring.FirebaseDataModule
import akio.apps.myrun.data.wiring.NetworkModule
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
                DaggerAuthenticationDataComponent.create()
        ): ExternalAppDataComponent
    }
}
