package akio.apps.myrun.feature.home.wiring

import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.domain.user.wiring.DaggerUserDomainComponent
import akio.apps.myrun.domain.user.wiring.UserDomainComponent
import akio.apps.myrun.feature.home.userhome.UserHomeViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    dependencies = [
        ActivityDataComponent::class,
        ActivityDomainComponent::class,
        UserDomainComponent::class
    ]
)
internal interface UserHomeFeatureComponent {
    fun userHomeViewModel(): UserHomeViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance savedStateHandle: SavedStateHandle,
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
            activityDomainComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
            userDomainComponent: UserDomainComponent = DaggerUserDomainComponent.factory().create()
        ): UserHomeFeatureComponent
    }
}
