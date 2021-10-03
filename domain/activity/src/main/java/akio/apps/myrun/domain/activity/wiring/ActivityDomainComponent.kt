package akio.apps.myrun.domain.activity.wiring

import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.domain.activity.impl.ActivityDateTimeFormatter
import akio.apps.myrun.domain.activity.impl.ExportTempTcxFileUsecase
import akio.apps.myrun.domain.activity.impl.GetFeedActivitiesUsecase
import akio.apps.myrun.domain.activity.impl.RunSplitsCalculator
import dagger.Component

@Component(
    dependencies = [
        LocationDataComponent::class,
        ActivityDataComponent::class,
        UserDataComponent::class,
        AuthenticationDataComponent::class
    ],
    modules = [
        DispatchersModule::class,
        ApplicationModule::class
    ]
)
interface ActivityDomainComponent {
    fun GetFeedActivitiesUsecase(): GetFeedActivitiesUsecase
    fun ExportTempTcxFileUsecase(): ExportTempTcxFileUsecase
    fun RunSplitsCalculator(): RunSplitsCalculator
    fun ActivityDateTimeFormatter(): ActivityDateTimeFormatter

    @Component.Factory
    interface Factory {
        fun create(
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create()
        ): ActivityDomainComponent
    }
}
