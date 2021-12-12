package akio.apps.myrun.feature.home.wiring

import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.domain.user.wiring.DaggerUserDomainComponent
import akio.apps.myrun.domain.user.wiring.UserDomainComponent
import akio.apps.myrun.feature.home.feed.ActivityFeedViewModel
import dagger.Component

@FeatureScope
@Component(
    dependencies = [
        ActivityDomainComponent::class,
        AuthenticationDataComponent::class,
        UserDomainComponent::class,
        UserDataComponent::class
    ],
    modules = [
        LaunchCatchingModule::class
    ]
)
internal interface ActivityFeedFeatureComponent {
    fun feedViewModel(): ActivityFeedViewModel

    @Component.Factory
    interface Factory {
        fun create(
            activityDomainComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDomainComponent: UserDomainComponent = DaggerUserDomainComponent.factory().create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create()
        ): ActivityFeedFeatureComponent
    }
}
