package akio.apps.myrun.feature.home._di

import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.feature.home.HomeViewModel
import akio.apps.myrun.feature.home.feed.ActivityFeedViewModel
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
    fun activityFeedViewModel(): ActivityFeedViewModel
    fun homeViewModel(): HomeViewModel

    @Component.Factory
    interface Factory {
        fun create(
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            domainComponent: DomainComponent = DaggerDomainComponent.factory().create(),
        ): HomeFeatureComponent
    }
}
