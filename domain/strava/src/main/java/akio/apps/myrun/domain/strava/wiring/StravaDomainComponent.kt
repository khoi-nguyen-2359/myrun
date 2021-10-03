package akio.apps.myrun.domain.strava.wiring

import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.eapps.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.eapps.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.domain.strava.impl.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.strava.impl.ExchangeStravaLoginCodeUsecase
import akio.apps.myrun.domain.strava.impl.RemoveStravaTokenUsecase
import akio.apps.myrun.domain.strava.impl.UpdateStravaTokenUsecase
import akio.apps.myrun.domain.strava.impl.UploadActivityFilesToStravaUsecase
import dagger.Component

@Component(
    dependencies = [
        ExternalAppDataComponent::class,
        ActivityDataComponent::class,
        AuthenticationDataComponent::class
    ],
    modules = [
        DispatchersModule::class
    ]
)
interface StravaDomainComponent {
    fun DeauthorizeStravaUsecase(): DeauthorizeStravaUsecase
    fun ExchangeStravaLoginCodeUsecase(): ExchangeStravaLoginCodeUsecase
    fun RemoveStravaTokenUsecase(): RemoveStravaTokenUsecase
    fun UpdateStravaTokenUsecase(): UpdateStravaTokenUsecase
    fun UploadActivityFilesToStravaUsecase(): UploadActivityFilesToStravaUsecase

    @Component.Factory
    interface Factory {
        fun create(
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(),
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create()
        ): StravaDomainComponent
    }
}
