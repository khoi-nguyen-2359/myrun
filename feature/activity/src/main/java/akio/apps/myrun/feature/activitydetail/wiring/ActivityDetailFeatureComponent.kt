package akio.apps.myrun.feature.activitydetail.wiring

import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.domain.user.wiring.DaggerUserDomainComponent
import akio.apps.myrun.domain.user.wiring.UserDomainComponent
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [
        ActivityDataComponent::class,
        ActivityDomainComponent::class,
        AuthenticationDataComponent::class,
        UserDataComponent::class,
        UserDomainComponent::class
    ]
)
internal interface ActivityDetailFeatureComponent {
    fun activityDetailsViewModel(): ActivityDetailViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance savedStateHandle: SavedStateHandle,
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            activityDomainComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
            userDomainComponent: UserDomainComponent = DaggerUserDomainComponent.factory().create()
        ): ActivityDetailFeatureComponent
    }
}
