package akio.apps.myrun.domain.strava.wiring

import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.eapps.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.eapps.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.domain.strava.impl.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.strava.impl.ExchangeStravaLoginCodeUsecase
import akio.apps.myrun.domain.strava.impl.RemoveStravaTokenUsecase
import akio.apps.myrun.domain.strava.impl.UpdateStravaTokenUsecase
import akio.apps.myrun.domain.strava.impl.UploadActivityFilesToStravaUsecase
import dagger.Component

@Component(
    dependencies = [
        ExternalAppDataComponent::class,
        ActivityDomainComponent::class,
        AuthenticationDataComponent::class
    ],
    modules = [
        DispatchersModule::class
    ]
)
interface StravaDomainComponent {
    fun deauthorizeStravaUsecase(): DeauthorizeStravaUsecase
    fun exchangeStravaLoginCodeUsecase(): ExchangeStravaLoginCodeUsecase
    fun removeStravaTokenUsecase(): RemoveStravaTokenUsecase
    fun updateStravaTokenUsecase(): UpdateStravaTokenUsecase
    fun uploadActivityFilesToStravaUsecase(): UploadActivityFilesToStravaUsecase

    @Component.Factory
    interface Factory {
        fun create(
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(),
            activityDomainComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create()
        ): StravaDomainComponent
    }
}
