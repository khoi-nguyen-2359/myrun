package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.wiring.data.activity.ActivityDataComponent
import akio.apps.myrun.wiring.data.activity.DaggerActivityDataComponent
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataComponent
import akio.apps.myrun.wiring.data.authentication.DaggerAuthenticationDataComponent
import akio.apps.myrun.wiring.data.user.DaggerUserDataComponent
import akio.apps.myrun.wiring.data.user.UserDataComponent
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        UserDataComponent::class,
        DomainComponent::class
    ]
)
interface ActivityDetailFeatureComponent {
    fun activityDetailsViewModel(): ActivityDetailViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance arguments: ActivityDetailViewModel.Arguments,
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            domainComponent: DomainComponent = DaggerDomainComponent.create(),
        ): ActivityDetailFeatureComponent
    }
}
