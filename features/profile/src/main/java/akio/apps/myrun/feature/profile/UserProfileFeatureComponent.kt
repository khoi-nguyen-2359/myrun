package akio.apps.myrun.feature.profile

import akio.apps.common.wiring.ApplicationModule
import akio.apps.common.wiring.FeatureScope
import akio.apps.common.wiring.LaunchCatchingModule
import akio.apps.myrun.wiring.data.activity.ActivityDataComponent
import akio.apps.myrun.wiring.data.activity.DaggerActivityDataComponent
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataComponent
import akio.apps.myrun.wiring.data.authentication.DaggerAuthenticationDataComponent
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        ApplicationModule::class
    ],
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        DomainComponent::class
    ]
)
interface UserProfileFeatureComponent {
    fun userProfileViewModel(): UserProfileViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance arguments: UserProfileViewModel.Arguments,
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            domainComponent: DomainComponent = DaggerDomainComponent.create(),
        ): UserProfileFeatureComponent
    }
}
