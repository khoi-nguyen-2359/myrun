package akio.apps.myrun.feature.home._di

import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.feature.home.HomeViewModel
import akio.apps.myrun.feature.usertimeline.impl.UserTimelineViewModel
import akio.apps.myrun.wiring.data.activity.ActivityDataComponent
import akio.apps.myrun.wiring.data.activity.DaggerActivityDataComponent
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataComponent
import akio.apps.myrun.wiring.data.authentication.DaggerAuthenticationDataComponent
import akio.apps.myrun.wiring.data.user.DaggerUserDataComponent
import akio.apps.myrun.wiring.data.user.UserDataComponent
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.Component

@Component(
    modules = [
        LaunchCatchingModule::class
    ],
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        UserDataComponent::class,
        DomainComponent::class
    ]
)
interface HomeFeatureComponent {
    fun userFeedViewModel(): UserTimelineViewModel
    fun homeViewModel(): HomeViewModel

    @Component.Factory
    interface Factory {
        fun create(
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            domainComponent: DomainComponent = DaggerDomainComponent.create(),
        ): HomeFeatureComponent
    }
}
