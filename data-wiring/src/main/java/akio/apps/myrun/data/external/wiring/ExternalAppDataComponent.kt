package akio.apps.myrun.data.external.wiring

import akio.apps.base.wiring.ApplicationModule
import akio.apps.myrun.data._base.wiring.FirebaseDataModule
import akio.apps.myrun.data._base.wiring.NetworkModule
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataModule
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaDataRepository
import akio.apps.myrun.data.externalapp.StravaTokenRepository
import dagger.Component

@Component(
    modules = [
        ExternalAppDataModule::class,
        FirebaseDataModule::class,
        NetworkModule::class,
        AuthenticationDataModule::class,
        ApplicationModule::class
    ]
)
interface ExternalAppDataComponent {
    fun stravaTokenRepository(): StravaTokenRepository
    fun externalAppRepository(): ExternalAppProvidersRepository
    fun stravaDataRepository(): StravaDataRepository
}
